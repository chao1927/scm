package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.domain.LogisticsExceptionAggregate;
import com.chaobo.scm.tms.domain.LogisticsFeeSourceAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LogisticsSettlementApplicationServiceTest {
    @Test
    void registerCloseExceptionGenerateAndPushFeeSource() {
        Services services = servicesWithWaybill();

        LogisticsSettlementMapper.ExceptionRow exception = services.exceptionService.register(
                new LogisticsExceptionApplicationService.RegisterCommand("WB800001", "DAMAGED", "P1",
                        "外包装破损", "CARRIER", 1001L, "idem-exc"));
        LogisticsSettlementMapper.ExceptionRow closed = services.exceptionService.close(exception.exceptionNo(),
                new LogisticsExceptionApplicationService.CloseCommand("已索赔", "CARRIER", exception.version(),
                        1001L, "idem-close"));
        LogisticsSettlementMapper.FeeSourceRow feeSource = services.feeSourceService.generate("WB800001",
                new LogisticsFeeSourceApplicationService.GenerateCommand("FREIGHT", new BigDecimal("12.30"),
                        "CNY", "202607", "SHIPPER", 1001L, "idem-fee"));
        LogisticsSettlementMapper.FeeSourceRow repeated = services.feeSourceService.generate("WB800001",
                new LogisticsFeeSourceApplicationService.GenerateCommand("FREIGHT", new BigDecimal("99.99"),
                        "CNY", "202607", "SHIPPER", 1001L, "idem-fee-repeat"));
        LogisticsSettlementMapper.FeeSourceRow pushed = services.feeSourceService.pushBms(feeSource.feeSourceNo(),
                new LogisticsFeeSourceApplicationService.PushCommand("BMS1", 1001L, "idem-push"));

        assertThat(closed.status()).isEqualTo(LogisticsExceptionAggregate.CLOSED);
        assertThat(repeated.feeSourceNo()).isEqualTo(feeSource.feeSourceNo());
        assertThat(pushed.pushStatus()).isEqualTo(LogisticsFeeSourceAggregate.PUSHED);
        assertThat(services.mapper.outbox).extracting(TransportTaskMapper.OutboxRow::eventType)
                .contains("LogisticsExceptionRegistered", "LogisticsExceptionClosed",
                        "LogisticsFeeSourceGenerated", "LogisticsFeeSourcePushed");
    }

    @Test
    void rejectCloseExceptionWithWrongVersion() {
        Services services = servicesWithWaybill();
        LogisticsSettlementMapper.ExceptionRow exception = services.exceptionService.register(
                new LogisticsExceptionApplicationService.RegisterCommand("WB800001", "DAMAGED", "P1",
                        "外包装破损", "CARRIER", 1001L, "idem-exc"));

        assertThatThrownBy(() -> services.exceptionService.close(exception.exceptionNo(),
                new LogisticsExceptionApplicationService.CloseCommand("已索赔", "CARRIER", 9, 1001L,
                        "idem-close")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version conflict");
    }

    public static Services servicesWithWaybill() {
        WaybillApplicationServiceTest.Services base = WaybillApplicationServiceTest.servicesWithAcceptedTask();
        WaybillMapper.WaybillRow waybill = base.waybillService().createFromTask("TMS700001",
                new WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123",
                        "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        if (!"WB800001".equals(waybill.waybillNo())) {
            throw new IllegalStateException("unexpected test waybill number");
        }
        MemoryLogisticsSettlementMapper mapper = new MemoryLogisticsSettlementMapper();
        LogisticsExceptionApplicationService exceptionService =
                new LogisticsExceptionApplicationService(mapper, base.waybillService());
        LogisticsFeeSourceApplicationService feeSourceService =
                new LogisticsFeeSourceApplicationService(mapper, base.waybillService());
        return new Services(mapper, exceptionService, feeSourceService);
    }

    public record Services(MemoryLogisticsSettlementMapper mapper,
                           LogisticsExceptionApplicationService exceptionService,
                           LogisticsFeeSourceApplicationService feeSourceService) {}

    public static class MemoryLogisticsSettlementMapper implements LogisticsSettlementMapper {
        final Map<String, ExceptionRow> exceptions = new LinkedHashMap<>();
        final Map<String, FeeSourceRow> feeSources = new LinkedHashMap<>();
        final List<TransportTaskMapper.OutboxRow> outbox = new ArrayList<>();
        final List<TransportTaskMapper.OperationLogRow> logs = new ArrayList<>();

        @Override
        public ExceptionRow findException(String exceptionNo) { return exceptions.get(exceptionNo); }

        @Override
        public List<ExceptionRow> listExceptions() { return new ArrayList<>(exceptions.values()); }

        @Override
        public void insertException(ExceptionRow row) { exceptions.put(row.exceptionNo(), row); }

        @Override
        public void updateException(ExceptionRow row) { exceptions.put(row.exceptionNo(), row); }

        @Override
        public FeeSourceRow findFeeSource(String feeSourceNo) { return feeSources.get(feeSourceNo); }

        @Override
        public FeeSourceRow findFeeSourceByWaybillAndItem(String waybillNo, String feeItemCode) {
            return feeSources.values().stream()
                    .filter(row -> row.waybillNo().equals(waybillNo))
                    .filter(row -> row.feeItemCode().equals(feeItemCode))
                    .findFirst().orElse(null);
        }

        @Override
        public List<FeeSourceRow> listFeeSources() { return new ArrayList<>(feeSources.values()); }

        @Override
        public void insertFeeSource(FeeSourceRow row) { feeSources.put(row.feeSourceNo(), row); }

        @Override
        public void updateFeeSource(FeeSourceRow row) { feeSources.put(row.feeSourceNo(), row); }

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
