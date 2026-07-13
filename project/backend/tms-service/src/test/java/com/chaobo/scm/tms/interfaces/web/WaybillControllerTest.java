package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.ShippingLabelApplicationService;
import com.chaobo.scm.tms.application.TransportTaskApplicationService;
import com.chaobo.scm.tms.application.TransportTaskApplicationServiceTest;
import com.chaobo.scm.tms.application.WaybillApplicationService;
import com.chaobo.scm.tms.application.WaybillApplicationServiceTest;
import com.chaobo.scm.tms.domain.ShippingLabelAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaybillControllerTest {
    @Test
    void createWaybillAndLabelThroughControllers() {
        TransportTaskApplicationServiceTest.MemoryTransportTaskMapper taskMapper =
                new TransportTaskApplicationServiceTest.MemoryTransportTaskMapper();
        TransportTaskApplicationService transportTaskService = new TransportTaskApplicationService(taskMapper);
        TransportTaskMapper.TaskRow task = transportTaskService.createFromSource(
                TransportTaskApplicationServiceTest.createCommand("idem-task"));
        transportTaskService.accept(task.taskNo(), new TransportTaskApplicationService.AcceptCommand(
                "SF", "顺丰", "SF-EXPRESS", task.version(), 1001L, "idem-accept"));
        WaybillApplicationServiceTest.MemoryWaybillMapper waybillMapper =
                new WaybillApplicationServiceTest.MemoryWaybillMapper();
        WaybillApplicationService waybillService = new WaybillApplicationService(waybillMapper, transportTaskService);
        ShippingLabelApplicationService labelService = new ShippingLabelApplicationService(waybillMapper, waybillService);
        WaybillController waybillController = new WaybillController(waybillService);
        ShippingLabelController labelController = new ShippingLabelController(labelService);

        WaybillMapper.WaybillRow waybill = waybillController.create(task.taskNo(),
                new WaybillController.CreateWaybillRequest("SF", "顺丰", "SF123",
                        "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        WaybillMapper.LabelRow label = labelController.generate(waybill.waybillNo(),
                new ShippingLabelController.GenerateLabelRequest("PKG1", "SF-V1",
                        "oss://labels/LBL1.pdf", 1001L, "idem-label"));
        WaybillMapper.LabelRow printed = labelController.print(label.labelNo(),
                new ShippingLabelController.PrintLabelRequest("PRINTER-1", 1001L, "idem-print"));

        assertThat(printed.status()).isEqualTo(ShippingLabelAggregate.PRINTED);
        assertThat(labelController.list(waybill.waybillNo())).hasSize(1);
    }
}
