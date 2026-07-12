package com.chaobo.scm.oms.domain;

import java.util.ArrayList;
import java.util.List;

public class OutboundAggregate {
    public static final int DRAFT = 1;
    public static final int ISSUED = 2;
    public static final int WMS_ACCEPTED = 3;
    public static final int PICKING = 4;
    public static final int SHIPPED = 5;
    public static final int CANCELLED = 6;
    public static final int EXCEPTION = 7;
    public static final int CANCEL_REQUESTED = 8;

    private final String outboundNo;
    private final String fulfillmentNo;
    private final String salesOrderNo;
    private final Long warehouseId;
    private final String warehouseCode;
    private String wmsOrderNo;
    private int status;
    private String cancelReason;
    private int retryCount;
    private long version;
    private final List<OmsEvent> events = new ArrayList<>();

    private OutboundAggregate(String outboundNo, String fulfillmentNo, String salesOrderNo, Long warehouseId,
                              String warehouseCode, String wmsOrderNo, int status, String cancelReason,
                              int retryCount, long version) {
        if (blank(outboundNo) || blank(fulfillmentNo) || blank(salesOrderNo) || warehouseId == null
                || warehouseId <= 0 || blank(warehouseCode)) {
            throw new IllegalArgumentException("outbound references and warehouse are required");
        }
        this.outboundNo = outboundNo;
        this.fulfillmentNo = fulfillmentNo;
        this.salesOrderNo = salesOrderNo;
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
        this.wmsOrderNo = wmsOrderNo;
        this.status = status;
        this.cancelReason = cancelReason;
        this.retryCount = retryCount;
        this.version = version;
    }

    public static OutboundAggregate create(String outboundNo, String fulfillmentNo, String salesOrderNo,
                                            Long warehouseId, String warehouseCode) {
        OutboundAggregate aggregate = new OutboundAggregate(outboundNo, fulfillmentNo, salesOrderNo, warehouseId,
                warehouseCode, null, DRAFT, null, 0, 1);
        aggregate.events.add(OmsEvent.of("OutboundOrderCreated", outboundNo, fulfillmentNo));
        return aggregate;
    }

    public static OutboundAggregate restore(String outboundNo, String fulfillmentNo, String salesOrderNo,
                                            Long warehouseId, String warehouseCode, String wmsOrderNo, int status,
                                            String cancelReason, int retryCount, long version) {
        return new OutboundAggregate(outboundNo, fulfillmentNo, salesOrderNo, warehouseId, warehouseCode,
                wmsOrderNo, status, cancelReason, retryCount, version);
    }

    public void dispatch() {
        if (status != DRAFT && status != EXCEPTION) {
            throw new IllegalStateException("outbound is not dispatchable");
        }
        status = ISSUED;
        retryCount++;
        version++;
        events.add(OmsEvent.of("OutboundInstructionIssued", outboundNo, fulfillmentNo));
    }

    public void retryDispatch() {
        if (status != ISSUED && status != EXCEPTION) {
            throw new IllegalStateException("outbound is not retryable");
        }
        status = ISSUED;
        retryCount++;
        version++;
        events.add(OmsEvent.of("OutboundRepushed", outboundNo, Integer.toString(retryCount)));
    }

    public void markWmsAccepted(String wmsOrderNo) {
        if (status != ISSUED) {
            throw new IllegalStateException("outbound is not issued");
        }
        if (blank(wmsOrderNo)) {
            throw new IllegalArgumentException("WMS order number is required");
        }
        this.wmsOrderNo = wmsOrderNo;
        status = WMS_ACCEPTED;
        version++;
        events.add(OmsEvent.of("WmsOutboundAccepted", outboundNo, wmsOrderNo));
    }

    public void markShipped() {
        if (status != WMS_ACCEPTED && status != PICKING) {
            throw new IllegalStateException("outbound is not in WMS processing");
        }
        status = SHIPPED;
        version++;
        events.add(OmsEvent.of("WmsOutboundShipped", outboundNo, wmsOrderNo));
    }

    public void requestCancel(String reason) {
        if (status == SHIPPED) {
            throw new IllegalStateException("shipped outbound cannot be cancelled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("cancel reason is required");
        }
        cancelReason = reason;
        status = CANCEL_REQUESTED;
        version++;
        events.add(OmsEvent.of("OutboundCancelRequested", outboundNo, reason));
    }

    public void markCancelled() {
        if (status != CANCEL_REQUESTED && status != DRAFT && status != ISSUED) {
            throw new IllegalStateException("outbound is not cancellable");
        }
        status = CANCELLED;
        version++;
        events.add(OmsEvent.of("WmsOutboundCancelled", outboundNo, wmsOrderNo == null ? "" : wmsOrderNo));
    }

    public List<OmsEvent> pullEvents() {
        List<OmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String outboundNo() { return outboundNo; }
    public String fulfillmentNo() { return fulfillmentNo; }
    public String salesOrderNo() { return salesOrderNo; }
    public Long warehouseId() { return warehouseId; }
    public String warehouseCode() { return warehouseCode; }
    public String wmsOrderNo() { return wmsOrderNo; }
    public int status() { return status; }
    public String cancelReason() { return cancelReason; }
    public int retryCount() { return retryCount; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
