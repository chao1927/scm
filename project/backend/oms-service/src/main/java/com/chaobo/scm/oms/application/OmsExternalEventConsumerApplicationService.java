package com.chaobo.scm.oms.application;

import org.springframework.stereotype.Service;

/**
 * OMS 外部事件分发服务。
 *
 * <p>只把标准信封转换为既有应用事件，各目标应用服务仍在自己的事务中执行 Inbox
 * 抢占、状态推进、成功确认和失败记录。
 */
@Service
public class OmsExternalEventConsumerApplicationService
        implements OmsExternalEventHandler {

    private final FulfillmentApplicationService fulfillment;
    private final CancellationApplicationService cancellation;
    private final AfterSaleApplicationService afterSale;
    private final ReverseAfterSaleApplicationService reverseAfterSale;

    public OmsExternalEventConsumerApplicationService(
            FulfillmentApplicationService fulfillment,
            CancellationApplicationService cancellation,
            AfterSaleApplicationService afterSale,
            ReverseAfterSaleApplicationService reverseAfterSale) {
        this.fulfillment = fulfillment;
        this.cancellation = cancellation;
        this.afterSale = afterSale;
        this.reverseAfterSale = reverseAfterSale;
    }

    @Override
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void consume(OmsExternalEvent event) {
        switch (event.eventType()) {
            case "StockReserved", "StockReservationFailed", "StockReleased" -> {
                    requireSource(event, "INVENTORY");
                    consumeFulfillment(event);
                }
            case "WmsOutboundAccepted", "WmsOutboundShipped",
                 "WmsOutboundCancelled" -> {
                    requireSource(event, "WMS");
                    consumeFulfillment(event);
                }
            case "RefundCompleted" -> {
                requireSource(event, "BMS");
                consumeRefund(event);
            }
            case "ReturnReceived", "ReturnInspected" -> {
                requireSource(event, "WMS");
                consumeReverseAfterSale(event);
            }
            case "ReshipFulfillmentCreated" -> {
                requireSource(event, "OMS");
                consumeReverseAfterSale(event);
            }
            default -> throw new IllegalArgumentException(
                    "unsupported OMS event: " + event.eventType());
        }
    }

    private static void requireSource(OmsExternalEvent event, String expected) {
        if (event.sourceSystem() == null
                || !expected.equalsIgnoreCase(event.sourceSystem().trim())) {
            throw new IllegalArgumentException(
                    "OMS 事件来源与类型不匹配: " + event.eventType());
        }
    }

    private void consumeFulfillment(OmsExternalEvent event) {
        fulfillment.consumeEvent(new FulfillmentApplicationService.ExternalEvent(
                event.eventCode(), event.eventType(), event.businessNo(),
                event.fulfillmentNo(), event.reservationRefNo(),
                event.reservationNo(), event.quantity(), event.outboundNo(),
                event.wmsOrderNo(), event.reason(), event.payload()));
        if ("WmsOutboundCancelled".equals(event.eventType())
                || "StockReleased".equals(event.eventType())) {
            cancellation.consumeEvent(new CancellationApplicationService.CancellationEvent(
                    event.eventCode() + ":cancellation", event.eventType(),
                    event.businessNo(), event.outboundNo(), event.reservationRefNo(),
                    event.payload()));
        }
    }

    private void consumeRefund(OmsExternalEvent event) {
        BigDecimalValue refund = new BigDecimalValue(
                event.amount() == null ? event.quantity() : event.amount());
        if (event.afterSaleNo() != null
                && event.afterSaleNo().startsWith("RAS")) {
            reverseAfterSale.consume(new ReverseAfterSaleApplicationService.Event(
                    event.eventCode(), event.eventType(), event.afterSaleNo(),
                    event.receivedQty(), event.acceptedQty(), refund.value(),
                    event.unmatched(), event.payload()));
            return;
        }
        afterSale.consumeEvent(new AfterSaleApplicationService.RefundEvent(
                event.eventCode(), event.eventType(), event.businessNo(),
                event.afterSaleNo(), refund.value(), event.payload()));
    }

    private void consumeReverseAfterSale(OmsExternalEvent event) {
        reverseAfterSale.consume(new ReverseAfterSaleApplicationService.Event(
                event.eventCode(), event.eventType(), event.afterSaleNo(),
                event.receivedQty(), event.acceptedQty(), event.amount(),
                event.unmatched(), event.payload()));
    }

    private record BigDecimalValue(java.math.BigDecimal value) {
        private BigDecimalValue {
            if (value == null) {
                throw new IllegalArgumentException("refund event amount is required");
            }
        }
    }
}
