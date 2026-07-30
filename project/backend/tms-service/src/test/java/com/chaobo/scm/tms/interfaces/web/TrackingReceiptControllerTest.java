package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.CarrierCallbackApplicationService;
import com.chaobo.scm.tms.application.DeliveryReceiptApplicationService;
import com.chaobo.scm.tms.application.TrackingApplicationService;
import com.chaobo.scm.tms.application.TrackingReceiptApplicationServiceTest;
import com.chaobo.scm.tms.application.WaybillApplicationServiceTest;
import com.chaobo.scm.tms.domain.DeliveryReceiptAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TrackingReceiptControllerTest。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class TrackingReceiptControllerTest {

    /**
     * 处理当前类型职责中的操作 {@code supplementAndCarrierReceiptWorkThroughControllers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void supplementAndCarrierReceiptWorkThroughControllers() {
        WaybillApplicationServiceTest.Services base = WaybillApplicationServiceTest.servicesWithAcceptedTask();
        base.waybillService().createFromTask("TMS700001", new com.chaobo.scm.tms.application.WaybillApplicationService.CreateCommand("SF", "顺丰", "SF123", "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        TrackingReceiptApplicationServiceTest.MemoryTrackingMapper mapper = new TrackingReceiptApplicationServiceTest.MemoryTrackingMapper();
        TrackingApplicationService trackingService = new TrackingApplicationService(mapper, base.waybillService());
        DeliveryReceiptApplicationService receiptService = new DeliveryReceiptApplicationService(mapper, base.waybillService());
        CarrierCallbackApplicationService callbackService = new CarrierCallbackApplicationService(mapper, trackingService, receiptService);
        TrackingController trackingController = new TrackingController(trackingService);
        DeliveryReceiptController receiptController = new DeliveryReceiptController(receiptService);
        CarrierCallbackController callbackController = new CarrierCallbackController(callbackService);
        TrackingMapper.TrackRow track = trackingController.supplement("WB800001", new TrackingController.SupplementTrackRequest("IN_TRANSIT", "人工补录在途", "嘉兴", LocalDateTime.parse("2026-07-12T11:00:00"), "承运商漏推", 1001L, "idem-track"));
        var carrier = UsernamePasswordAuthenticationToken.authenticated("sf", "n/a", java.util.List.of());
        carrier.setDetails(new ScmAccessContext(1, "sf", "SF", java.util.Set.of("tms:carrier-callback:write"), java.util.Map.of()));
        callbackController.consume("SF", new CarrierCallbackController.CarrierCallbackRequest("evt-sign-1", "SIGNED", "WB800001", null, null, null, LocalDateTime.parse("2026-07-12T12:00:00"), DeliveryReceiptAggregate.SIGNED, "李四", null, "oss://proof/RCP1.jpg", 1001L, "{}"), carrier);
        assertThat(track.nodeCode()).isEqualTo("IN_TRANSIT");
        assertThat(trackingController.list("WB800001")).hasSize(1);
        assertThat(receiptController.get("RCP110001")).isNotNull();
    }
}
