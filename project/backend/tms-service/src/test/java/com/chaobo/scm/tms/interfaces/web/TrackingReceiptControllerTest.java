package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.CarrierCallbackApplicationService;
import com.chaobo.scm.tms.application.DeliveryReceiptApplicationService;
import com.chaobo.scm.tms.application.TrackingApplicationService;
import com.chaobo.scm.tms.application.TrackingReceiptApplicationServiceTest;
import com.chaobo.scm.tms.application.WaybillApplicationServiceTest;
import com.chaobo.scm.tms.domain.DeliveryReceiptAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TrackingReceiptControllerTest {
    @Test
    void supplementAndCarrierReceiptWorkThroughControllers() {
        WaybillApplicationServiceTest.Services base = WaybillApplicationServiceTest.servicesWithAcceptedTask();
        base.waybillService().createFromTask("TMS700001",
                new com.chaobo.scm.tms.application.WaybillApplicationService.CreateCommand("SF", "顺丰",
                        "SF123", "SF-EXPRESS", "ok", 1001L, "idem-wb"));
        TrackingReceiptApplicationServiceTest.MemoryTrackingMapper mapper =
                new TrackingReceiptApplicationServiceTest.MemoryTrackingMapper();
        TrackingApplicationService trackingService = new TrackingApplicationService(mapper, base.waybillService());
        DeliveryReceiptApplicationService receiptService = new DeliveryReceiptApplicationService(mapper,
                base.waybillService());
        CarrierCallbackApplicationService callbackService = new CarrierCallbackApplicationService(mapper,
                trackingService, receiptService);
        TrackingController trackingController = new TrackingController(trackingService);
        DeliveryReceiptController receiptController = new DeliveryReceiptController(receiptService);
        CarrierCallbackController callbackController = new CarrierCallbackController(callbackService);

        TrackingMapper.TrackRow track = trackingController.supplement("WB800001",
                new TrackingController.SupplementTrackRequest("IN_TRANSIT", "人工补录在途", "嘉兴",
                        LocalDateTime.parse("2026-07-12T11:00:00"), "承运商漏推", 1001L, "idem-track"));
        callbackController.consume("SF", new CarrierCallbackController.CarrierCallbackRequest("evt-sign-1",
                "SIGNED", "WB800001", null, null, null, LocalDateTime.parse("2026-07-12T12:00:00"),
                DeliveryReceiptAggregate.SIGNED, "李四", null, "oss://proof/RCP1.jpg", 1001L, "{}"));

        assertThat(track.nodeCode()).isEqualTo("IN_TRANSIT");
        assertThat(trackingController.list("WB800001")).hasSize(1);
        assertThat(receiptController.get("RCP110001")).isNotNull();
    }
}
