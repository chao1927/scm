package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.ShippingLabelAggregate;
import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.domain.WaybillAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WaybillApplicationServiceTest {
    @Test
    void createWaybillGenerateAndPrintLabel() {
        Services services = servicesWithAcceptedTask();

        WaybillMapper.WaybillRow waybill = services.waybillService.createFromTask("TMS700001",
                new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123",
                        "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        WaybillMapper.WaybillRow repeated = services.waybillService.createFromTask("TMS700001",
                new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF999",
                        "SF-EXPRESS", "repeat", 1001L, "idem-repeat"));
        WaybillMapper.LabelRow label = services.labelService.generate(waybill.waybillNo(),
                new ShippingLabelApplicationService.GenerateCommand("PKG1", "SF-V1",
                        "oss://labels/LBL1.pdf", 1001L, "idem-label"));
        WaybillMapper.LabelRow printed = services.labelService.print(label.labelNo(),
                new ShippingLabelApplicationService.PrintCommand("PRINTER-1", 1001L, "idem-print"));

        assertThat(repeated.waybillNo()).isEqualTo(waybill.waybillNo());
        assertThat(waybill.status()).isEqualTo(WaybillAggregate.CREATED);
        assertThat(printed.status()).isEqualTo(ShippingLabelAggregate.PRINTED);
        assertThat(services.waybillMapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType)
                .contains("WaybillCreated", "ShippingLabelGenerated", "ShippingLabelPrinted");
        assertThat(services.waybillMapper.logs).extracting(TransportTaskMapper.OperationLogRow::operationType)
                .contains("CREATE_WAYBILL", "GENERATE_SHIPPING_LABEL", "PRINT_SHIPPING_LABEL");
    }

    @Test
    void voidWaybillRejectsStaleVersion() {
        Services services = servicesWithAcceptedTask();
        WaybillMapper.WaybillRow waybill = services.waybillService.createFromTask("TMS700001",
                new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123",
                        "SF-EXPRESS", "ok", 1001L, "idem-wb"));

        assertThatThrownBy(() -> services.waybillService.voidWaybill(waybill.waybillNo(),
                new WaybillApplicationService.VoidCommand("客户取消", "APR1", 9, 1001L, "idem-void")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
    }

    public static Services servicesWithAcceptedTask() {
        TransportTaskApplicationServiceTest.MemoryTransportTaskMapper taskMapper =
                new TransportTaskApplicationServiceTest.MemoryTransportTaskMapper();
        TransportTaskApplicationService transportTaskService = new TransportTaskApplicationService(taskMapper);
        TransportTaskMapper.TaskRow created = transportTaskService.createFromSource(
                TransportTaskApplicationServiceTest.createCommand("idem-task"));
        transportTaskService.accept(created.taskNo(), new TransportTaskApplicationService.AcceptCommand(
                "SF", "顺丰", "SF-EXPRESS", created.version(), 1001L, "idem-accept"));
        MemoryWaybillMapper waybillMapper = new MemoryWaybillMapper();
        WaybillApplicationService waybillService = new WaybillApplicationService(waybillMapper, transportTaskService);
        ShippingLabelApplicationService labelService = new ShippingLabelApplicationService(waybillMapper, waybillService);
        return new Services(waybillMapper, waybillService, labelService);
    }

    public record Services(MemoryWaybillMapper waybillMapper, WaybillApplicationService waybillService,
                           ShippingLabelApplicationService labelService) {}

    public static class MemoryWaybillMapper implements WaybillMapper {
        final Map<String, WaybillRow> waybills = new LinkedHashMap<>();
        final Map<String, LabelRow> labels = new LinkedHashMap<>();
        final List<TransportTaskMapper.OutboxRow> outbox = new ArrayList<>();
        final List<TransportTaskMapper.OperationLogRow> logs = new ArrayList<>();

        @Override
        public WaybillRow findWaybill(String waybillNo) { return waybills.get(waybillNo); }

        @Override
        public WaybillRow findActiveWaybillByTask(String taskNo) {
            return waybills.values().stream()
                    .filter(row -> row.taskNo().equals(taskNo))
                    .filter(row -> row.status() != WaybillAggregate.VOIDED)
                    .findFirst().orElse(null);
        }

        @Override
        public List<WaybillRow> listWaybills() { return new ArrayList<>(waybills.values()); }

        @Override
        public void insertWaybill(WaybillRow row) { waybills.put(row.waybillNo(), row); }

        @Override
        public void updateWaybill(WaybillRow row) { waybills.put(row.waybillNo(), row); }

        @Override
        public LabelRow findLabel(String labelNo) { return labels.get(labelNo); }

        @Override
        public LabelRow findActiveLabel(String waybillNo, String packageNo) {
            return labels.values().stream()
                    .filter(row -> row.waybillNo().equals(waybillNo))
                    .filter(row -> row.packageNo().equals(packageNo))
                    .filter(row -> row.status() != ShippingLabelAggregate.VOIDED)
                    .findFirst().orElse(null);
        }

        @Override
        public List<LabelRow> listLabelsByWaybill(String waybillNo) {
            return labels.values().stream().filter(row -> row.waybillNo().equals(waybillNo)).toList();
        }

        @Override
        public void insertLabel(LabelRow row) { labels.put(row.labelNo(), row); }

        @Override
        public void updateLabel(LabelRow row) { labels.put(row.labelNo(), row); }

        @Override
        public void insertOutbox(TransportTaskMapper.OutboxRow row) { outbox.add(row); }

        @Override
        public List<TransportTaskMapper.OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(TransportTaskMapper.OperationLogRow row) {
            logs.add(new TransportTaskMapper.OperationLogRow(row.operationType(), row.businessNo(), row.operatorId(),
                    row.idempotencyKey(), LocalDateTime.now()));
        }

        @Override
        public List<TransportTaskMapper.OperationLogRow> listOperationLogs() { return logs; }
    }
}
