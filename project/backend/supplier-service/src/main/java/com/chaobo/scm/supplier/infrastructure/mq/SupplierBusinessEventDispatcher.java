package com.chaobo.scm.supplier.infrastructure.mq;

import com.chaobo.scm.supplier.application.asn.event.*;
import com.chaobo.scm.supplier.application.finance.*;
import com.chaobo.scm.supplier.application.operations.*;
import com.chaobo.scm.supplier.application.order.*;
import com.chaobo.scm.supplier.application.quality.*;
import com.chaobo.scm.supplier.application.returning.*;
import com.chaobo.scm.supplier.application.rfq.*;
import com.chaobo.scm.supplier.application.score.*;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Set;

/**
 * 将标准 RocketMQ 信封路由到已有应用消费服务；Inbox 幂等仍由各应用服务负责。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SupplierBusinessEventDispatcher {

    private static final Set<String> PURCHASE_ORDER_EVENTS = Set.of(
            "PurchaseOrderReleased", "PurchaseOrderChanged", "PurchaseOrderCancelled", "PurchaseOrderClosed");
    private static final Set<String> RFQ_EVENTS = Set.of("RfqPublished", "RfqBiddingClosed");
    private static final Set<String> WMS_EVENTS = Set.of(
            "WmsAppointmentConfirmed", "WmsArrivalRegistered", "WmsReceiptCompleted");
    private static final Set<String> TMS_EVENTS = Set.of(
            "TransportTaskCreated", "WaybillAssigned", "TransportInTransit", "TransportArrived",
            "TransportException");
    private static final Set<String> RETURN_EVENTS = Set.of(
            "InventoryReturnLocked", "InventoryReturnLockFailed", "WmsSupplierReturnOutboundCompleted",
            "TmsSupplierReturnWaybillCreated", "TmsSupplierReturnSigned", "TmsSupplierReturnRejected",
            "TmsSupplierReturnTransportException", "BmsSupplierReturnSettlementCompleted");
    private static final Set<String> BMS_EVENTS = Set.of(
            "BmsReconciliationIssued", "BmsReconciliationChanged", "BmsReconciliationClosed",
            "BmsInvoiceValidated");
    private static final Set<String> OPERATIONS_EVENTS = Set.of(
            "RfqPublished", "SupplierContractSubmitted", "PurchaseOrderReleased",
            "SupplierRectificationRequested", "BmsReconciliationIssued",
            "SupplierReturnConfirmationRequested", "SupplierQualificationExpiring",
            "SupplierContractExpiring", "SupplierQuoteExpiring", "AsnDelayed",
            "TmsShipmentDelayed", "SupplierRectificationOverdue", "SupplierScorePublished");
    private static final String ISSUE_TYPE = "issueType";
    private static final String SEVERITY = "severity";
    private static final String METRIC_VALUE = "metricValue";

    private final ObjectMapper json;
    private final PurchaseOrderEventConsumerApplicationService purchaseOrders;
    private final RfqEventConsumerApplicationService rfqs;
    private final WmsAsnEventConsumerApplicationService wms;
    private final TmsAsnEventConsumerApplicationService tms;
    private final QualitySourceEventConsumerApplicationService quality;
    private final SupplierReturnEventConsumerApplicationService returns;
    private final BmsFinanceEventConsumerApplicationService finance;
    private final PerformanceFactEventConsumerApplicationService performance;
    private final OperationsEventConsumerApplicationService operations;

    public SupplierBusinessEventDispatcher(ObjectMapper json,
            PurchaseOrderEventConsumerApplicationService purchaseOrders,
            RfqEventConsumerApplicationService rfqs, WmsAsnEventConsumerApplicationService wms,
            TmsAsnEventConsumerApplicationService tms, QualitySourceEventConsumerApplicationService quality,
            SupplierReturnEventConsumerApplicationService returns,
            BmsFinanceEventConsumerApplicationService finance,
            PerformanceFactEventConsumerApplicationService performance,
            OperationsEventConsumerApplicationService operations) {
        this.json = json;
        this.purchaseOrders = purchaseOrders;
        this.rfqs = rfqs;
        this.wms = wms;
        this.tms = tms;
        this.quality = quality;
        this.returns = returns;
        this.finance = finance;
        this.performance = performance;
        this.operations = operations;
    }

    /**
     * 路由一个信封。无法识别的业务事件直接失败，禁止静默确认。
     */
    public void dispatch(SupplierBusinessEventEnvelopeCodec.Envelope envelope) {
        String type = envelope.eventType();
        boolean handled = false;
        if (PURCHASE_ORDER_EVENTS.contains(type)) {
            purchaseOrders.consume(payload(envelope, PurchaseOrderEvent.class));
            handled = true;
        }
        if (RFQ_EVENTS.contains(type)) {
            rfqs.consume(payload(envelope, RfqEvent.class));
            handled = true;
        }
        if (WMS_EVENTS.contains(type)) {
            wms.consume(payload(envelope, WmsAsnEvent.class));
            handled = true;
        }
        if (TMS_EVENTS.contains(type)) {
            tms.consume(payload(envelope, TmsAsnEvent.class));
            handled = true;
        }
        if (RETURN_EVENTS.contains(type)) {
            returns.consume(payload(envelope, SupplierReturnExternalEvent.class));
            handled = true;
        }
        if (BMS_EVENTS.contains(type)) {
            finance.consume(payload(envelope, BmsFinanceEvent.class));
            handled = true;
        }
        if (envelope.data().has(ISSUE_TYPE) && envelope.data().has(SEVERITY)) {
            quality.consume(payload(envelope, QualitySourceEvent.class));
            handled = true;
        }
        if (envelope.data().has(METRIC_VALUE)) {
            performance.consume(payload(envelope, PerformanceFactEvent.class));
            handled = true;
        }
        if (OPERATIONS_EVENTS.contains(type)) {
            operations.consume(payload(envelope, OperationsEvent.class));
            handled = true;
        }
        if (!handled) {
            throw new IllegalArgumentException("不支持的供应商业务事件: " + type);
        }
    }

    private <T> T payload(SupplierBusinessEventEnvelopeCodec.Envelope envelope, Class<T> type) {
        ObjectNode data = (ObjectNode) envelope.data().deepCopy();
        data.put("sourceSystem", envelope.sourceSystem());
        data.put("eventCode", envelope.eventCode());
        data.put("eventType", envelope.eventType());
        return json.readValue(data.toString(), type);
    }
}
