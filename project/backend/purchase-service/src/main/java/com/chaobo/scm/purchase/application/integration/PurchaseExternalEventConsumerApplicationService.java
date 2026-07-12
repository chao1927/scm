package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.inbound.InboundCommands;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingApplicationService;
import com.chaobo.scm.purchase.application.order.PurchaseOrderApplicationService;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.PurchaseExternalFactMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class PurchaseExternalEventConsumerApplicationService implements InboundEventReplayHandler {
    public static final String CONSUMER = "purchase-external-event";

    private final InboundEventLogPort inbox;
    private final InboundEventPayloadStore payloads;
    private final PurchaseExternalFactMapper facts;
    private final InboundTrackingApplicationService inbounds;
    private final PurchaseOrderApplicationService purchaseOrders;
    private final ObjectMapper json;

    public PurchaseExternalEventConsumerApplicationService(InboundEventLogPort inbox,
                                                           InboundEventPayloadStore payloads,
                                                           PurchaseExternalFactMapper facts,
                                                           InboundTrackingApplicationService inbounds,
                                                           PurchaseOrderApplicationService purchaseOrders,
                                                           ObjectMapper json) {
        this.inbox = inbox;
        this.payloads = payloads;
        this.facts = facts;
        this.inbounds = inbounds;
        this.purchaseOrders = purchaseOrders;
        this.json = json;
    }

    @Transactional
    public void consume(PurchaseExternalEvent event) {
        if (event.sourceSystem() == null || event.eventCode() == null || event.eventType() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "外部事件来源、编码和类型不能为空");
        }
        var key = event.sourceSystem() + ":" + event.eventCode();
        var claim = inbox.claim(event.sourceSystem(), event.eventCode(), event.eventType(), CONSUMER, key);
        if (claim == InboundEventLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return;
        }
        if (claim == InboundEventLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "采购外部事件正在处理");
        }
        payloads.save(event.sourceSystem(), event.eventCode(), CONSUMER, event);
        try {
            dispatch(event);
            inbox.markSucceeded(event.sourceSystem(), event.eventCode(), CONSUMER, false);
        } catch (RuntimeException exception) {
            inbox.recordFailure(event.sourceSystem(), event.eventCode(), event.eventType(), CONSUMER, key,
                    exception.getMessage());
            throw exception;
        }
    }

    @Override
    public String consumerName() {
        return CONSUMER;
    }

    @Override
    public void replay(InboundEventLogPort.ReplayEvent event) {
        try {
            consume(json.readValue(event.payloadJson(), PurchaseExternalEvent.class));
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购外部事件载荷无法反序列化");
        }
    }

    private void dispatch(PurchaseExternalEvent event) {
        switch (event.eventType()) {
            case "SupplierQuoteSubmitted", "SupplierQuoteChanged", "SupplierQuoteVoided", "SupplierQuoteAdopted" ->
                    facts.upsertQuote(event.eventCode(), text(event.quoteNo(), event.businessNo()), event.rfqNo(),
                            requiredLong(event.supplierId(), "供应商ID"), event.skuCode(), zero(event.quantity()),
                            zero(event.amount()), event.currency(), event.eventType(), write(event.payload()));
            case "PurchaseOrderConfirmed", "PurchaseOrderConfirmedBySupplier" -> consumeSupplierResponse(event,
                    PurchaseOrderApplicationService.SupplierResponseType.CONFIRMED);
            case "PurchaseOrderRejected", "PurchaseOrderRejectedBySupplier" -> consumeSupplierResponse(event,
                    PurchaseOrderApplicationService.SupplierResponseType.REJECTED);
            case "PurchaseOrderDifferenceSubmitted", "PurchaseOrderDifferenceReportedBySupplier",
                    "PurchaseOrderDiffFeedbackBySupplier" -> consumeSupplierResponse(event,
                    PurchaseOrderApplicationService.SupplierResponseType.DIFFERENCE);
            case "WmsReceiptCompleted", "WmsQualityInspectionCompleted", "WmsPutawayCompleted" -> consumeWms(event);
            case "TmsTransportTaskCreated", "TmsWaybillAssigned", "TmsTransportInTransit",
                    "TmsTransportArrived", "TmsTransportException" ->
                    facts.upsertTransport(event.eventCode(), text(event.orderNo(), event.businessNo()),
                            event.inboundNo(), event.asnNo(), event.shipmentId(), event.waybillNo(),
                            event.carrierCode(), event.eventType(), event.transportNode(), event.reason(),
                            occurredAt(event), write(event.payload()));
            case "BmsPayableCreated", "BmsPayableChanged", "BmsPayableClosed", "BmsSettlementCompleted" ->
                    facts.upsertBms(event.eventCode(), text(event.orderNo(), event.businessNo()),
                            requiredLong(event.supplierId(), "供应商ID"), event.eventType(), event.currency(),
                            zero(event.amount()), version(event), write(event.payload()));
            default -> inbox.markSucceeded(event.sourceSystem(), event.eventCode(), CONSUMER, true);
        }
    }

    private void consumeWms(PurchaseExternalEvent event) {
        facts.upsertWmsInbound(event.eventCode(), text(event.inboundNo(), event.businessNo()), event.orderNo(),
                event.asnNo(), event.warehouseCode(), event.eventType(), zero(event.receivedQty()),
                zero(event.qualifiedQty()), zero(event.unqualifiedQty()), zero(event.putawayQty()),
                event.reason(), occurredAt(event), write(event.payload()));
        if (event.inboundNo() != null && !event.inboundNo().isBlank()) {
            var context = new CommandContext(0, event.sourceSystem(), 0, null, event.eventCode(), null,
                    event.sourceSystem() + ":" + event.eventCode(), Set.of("purchase:inbound:sync-wms"));
            inbounds.syncWmsFromEvent(event.inboundNo(), new InboundCommands.SyncWms(
                    version(event), zero(event.receivedQty()), zero(event.qualifiedQty()),
                    zero(event.unqualifiedQty()), zero(event.putawayQty()), event.reason()), context);
        }
    }

    private void consumeSupplierResponse(PurchaseExternalEvent event,
                                         PurchaseOrderApplicationService.SupplierResponseType responseType) {
        var orderNo = orderNo(event);
        var supplierId = requiredLong(event.supplierId(), "供应商ID");
        facts.upsertSupplierConfirm(event.eventCode(), orderNo, supplierId, event.eventType(), event.reason(),
                version(event), occurredAt(event), write(event.payload()));
        var context = new CommandContext(0, event.sourceSystem(), 0, event.purchaseOrgId(), event.eventCode(), null,
                event.sourceSystem() + ":" + event.eventCode(), Set.of());
        purchaseOrders.recordSupplierResponse(orderNo, supplierId, responseType, event.reason(), context);
    }

    private String write(Map<String, Object> value) {
        try {
            return json.writeValueAsString(value == null ? Map.of() : value);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "外部事件扩展载荷序列化失败");
        }
    }

    private static String text(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first;
    }

    private static String orderNo(PurchaseExternalEvent event) {
        var orderNo = text(event.orderNo(), event.businessNo());
        if (orderNo == null || orderNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "供应商订单确认事件缺少采购订单号");
        }
        return orderNo;
    }

    private static long requiredLong(Long value, String name) {
        if (value == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, name + "不能为空");
        }
        return value;
    }

    private static int version(PurchaseExternalEvent event) {
        return event.sourceVersion() == null ? 0 : event.sourceVersion();
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static OffsetDateTime occurredAt(PurchaseExternalEvent event) {
        return event.occurredAt() == null ? OffsetDateTime.now() : event.occurredAt();
    }
}
