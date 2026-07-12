package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.FulfillmentApplicationService;
import com.chaobo.scm.oms.application.AfterSaleApplicationService;
import com.chaobo.scm.oms.application.CancellationApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/oms/v1")
public class OmsInternalEventController {
    private final FulfillmentApplicationService service;
    private final CancellationApplicationService cancellationService;
    private final AfterSaleApplicationService afterSaleService;

    public OmsInternalEventController(FulfillmentApplicationService service,
                                      CancellationApplicationService cancellationService,
                                      AfterSaleApplicationService afterSaleService) {
        this.service = service;
        this.cancellationService = cancellationService;
        this.afterSaleService = afterSaleService;
    }

    @PostMapping("/events")
    public void consume(@RequestBody EventRequest event) {
        switch (event.eventType()) {
            case "StockReserved", "StockReservationFailed", "StockReleased", "WmsOutboundAccepted",
                    "WmsOutboundShipped", "WmsOutboundCancelled" -> {
                service.consumeEvent(new FulfillmentApplicationService.ExternalEvent(event.eventId(),
                        event.eventType(), event.businessNo(), event.fulfillmentNo(), event.reservationRefNo(),
                        event.reservationNo(), event.quantity(), event.outboundNo(), event.wmsOrderNo(),
                        event.reason(), event.payload()));
                if (event.eventType().equals("WmsOutboundCancelled") || event.eventType().equals("StockReleased")) {
                    cancellationService.consumeEvent(new CancellationApplicationService.CancellationEvent(
                            event.eventId() + ":cancellation", event.eventType(), event.businessNo(),
                            event.outboundNo(), event.reservationRefNo(), event.payload()));
                }
            }
            case "RefundCompleted" -> afterSaleService.consumeEvent(new AfterSaleApplicationService.RefundEvent(
                    event.eventId(), event.eventType(), event.businessNo(), event.afterSaleNo(),
                    event.quantity(), event.payload()));
            default -> throw new IllegalArgumentException("unsupported OMS event: " + event.eventType());
        }
    }

    public record EventRequest(String eventId, String eventType, String businessNo, String fulfillmentNo,
                               String reservationRefNo, String reservationNo, java.math.BigDecimal quantity,
                               String outboundNo, String wmsOrderNo, String afterSaleNo, String reason,
                               String payload) {
    }
}
