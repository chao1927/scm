package com.chaobo.scm.oms.domain;

import java.util.ArrayList;
import java.util.List;

public class CancellationRequestAggregate {
    public static final int PENDING_REVIEW = 1;
    public static final int APPROVED = 2;
    public static final int PROCESSING = 3;
    public static final int COMPLETED = 4;
    public static final int REJECTED = 5;
    public static final int AFTER_SALE = 6;

    private final String cancellationNo;
    private final String salesOrderNo;
    private final String fulfillmentNo;
    private final String outboundNo;
    private final String reservationRefNo;
    private final String reason;
    private int status;
    private boolean wmsCancelled;
    private boolean stockReleased;
    private long version;
    private final List<OmsEvent> events = new ArrayList<>();

    private CancellationRequestAggregate(String cancellationNo, String salesOrderNo, String fulfillmentNo,
                                         String outboundNo, String reservationRefNo, String reason, int status,
                                         boolean wmsCancelled, boolean stockReleased, long version) {
        if (blank(cancellationNo) || blank(salesOrderNo) || blank(fulfillmentNo) || blank(reason)) {
            throw new IllegalArgumentException("cancellation references and reason are required");
        }
        this.cancellationNo = cancellationNo;
        this.salesOrderNo = salesOrderNo;
        this.fulfillmentNo = fulfillmentNo;
        this.outboundNo = outboundNo;
        this.reservationRefNo = reservationRefNo;
        this.reason = reason;
        this.status = status;
        this.wmsCancelled = wmsCancelled;
        this.stockReleased = stockReleased;
        this.version = version;
    }

    public static CancellationRequestAggregate create(String cancellationNo, String salesOrderNo,
                                                       String fulfillmentNo, String outboundNo,
                                                       String reservationRefNo, String reason) {
        CancellationRequestAggregate aggregate = new CancellationRequestAggregate(cancellationNo, salesOrderNo,
                fulfillmentNo, outboundNo, reservationRefNo, reason, PENDING_REVIEW, false, false, 1);
        aggregate.events.add(OmsEvent.of("CancelRequestCreated", cancellationNo, salesOrderNo));
        return aggregate;
    }

    public static CancellationRequestAggregate restore(String cancellationNo, String salesOrderNo,
                                                        String fulfillmentNo, String outboundNo,
                                                        String reservationRefNo, String reason, int status,
                                                        boolean wmsCancelled, boolean stockReleased, long version) {
        return new CancellationRequestAggregate(cancellationNo, salesOrderNo, fulfillmentNo, outboundNo,
                reservationRefNo, reason, status, wmsCancelled, stockReleased, version);
    }

    public void approve(String remark) {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("cancellation is not pending review");
        }
        if (blank(remark)) {
            throw new IllegalArgumentException("approval remark is required");
        }
        status = APPROVED;
        version++;
        events.add(OmsEvent.of("CancelRequestApproved", cancellationNo, remark));
    }

    public void process(boolean requiresWms) {
        if (status != APPROVED) {
            throw new IllegalStateException("cancellation is not approved");
        }
        status = PROCESSING;
        version++;
        if (requiresWms) {
            events.add(OmsEvent.of("WmsCancelRequested", cancellationNo, outboundNo));
        } else {
            events.add(OmsEvent.of("StockReleaseRequested", cancellationNo, reservationRefNo));
        }
    }

    public void markWmsCancelled() {
        if (status != PROCESSING) {
            throw new IllegalStateException("cancellation is not processing");
        }
        wmsCancelled = true;
        version++;
        events.add(OmsEvent.of("StockReleaseRequested", cancellationNo, reservationRefNo));
    }

    public void markStockReleased() {
        if (status != PROCESSING) {
            throw new IllegalStateException("cancellation is not processing");
        }
        if (outboundNo != null && !outboundNo.isBlank() && !wmsCancelled) {
            throw new IllegalStateException("WMS cancellation must complete before stock release");
        }
        stockReleased = true;
        status = COMPLETED;
        version++;
        events.add(OmsEvent.of("SalesOrderCanceled", cancellationNo, salesOrderNo));
    }

    public void reject(String remark) {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("cancellation is not pending review");
        }
        if (blank(remark)) {
            throw new IllegalArgumentException("reject remark is required");
        }
        status = REJECTED;
        version++;
        events.add(OmsEvent.of("CancelRequestRejected", cancellationNo, remark));
    }

    public List<OmsEvent> pullEvents() {
        List<OmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String cancellationNo() { return cancellationNo; }
    public String salesOrderNo() { return salesOrderNo; }
    public String fulfillmentNo() { return fulfillmentNo; }
    public String outboundNo() { return outboundNo; }
    public String reservationRefNo() { return reservationRefNo; }
    public String reason() { return reason; }
    public int status() { return status; }
    public boolean wmsCancelled() { return wmsCancelled; }
    public boolean stockReleased() { return stockReleased; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
