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

/**
 * WaybillControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WaybillControllerTest {

    /**
     * 执行命令 {@code createWaybillAndLabelThroughControllers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createWaybillAndLabelThroughControllers() {
        TransportTaskApplicationServiceTest.MemoryTransportTaskMapper taskMapper = new TransportTaskApplicationServiceTest.MemoryTransportTaskMapper();
        TransportTaskApplicationService transportTaskService = new TransportTaskApplicationService(taskMapper);
        TransportTaskMapper.TaskRow task = transportTaskService.createFromSource(TransportTaskApplicationServiceTest.createCommand("idem-task"));
        transportTaskService.accept(task.taskNo(), new TransportTaskApplicationService.AcceptCommand("SF", "顺丰", "SF-EXPRESS", task.version(), 1001L, "idem-accept"));
        WaybillApplicationServiceTest.MemoryWaybillMapper waybillMapper = new WaybillApplicationServiceTest.MemoryWaybillMapper();
        WaybillApplicationService waybillService = new WaybillApplicationService(waybillMapper, transportTaskService);
        ShippingLabelApplicationService labelService = new ShippingLabelApplicationService(waybillMapper, waybillService);
        WaybillController waybillController = new WaybillController(waybillService);
        ShippingLabelController labelController = new ShippingLabelController(labelService);
        WaybillMapper.WaybillRow waybill = waybillController.create(task.taskNo(), new WaybillController.CreateWaybillRequest("SF", "顺丰", "SF123", "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        WaybillMapper.LabelRow label = labelController.generate(waybill.waybillNo(), new ShippingLabelController.GenerateLabelRequest("PKG1", "SF-V1", "oss://labels/LBL1.pdf", 1001L, "idem-label"));
        WaybillMapper.LabelRow printed = labelController.print(label.labelNo(), new ShippingLabelController.PrintLabelRequest("PRINTER-1", 1001L, "idem-print"));
        assertThat(printed.status()).isEqualTo(ShippingLabelAggregate.PRINTED);
        assertThat(labelController.list(waybill.waybillNo())).hasSize(1);
    }
}
