package com.chaobo.scm.oms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AfterSaleAggregate {
    public static final int PENDING_REVIEW = 1;
    public static final int APPROVED = 2;
    public static final int REFUND_REQUESTED = 3;
    public static final int REFUNDED = 4;
    public static final int COMPLETED = 5;
    public static final int REJECTED = 6;

    private final String afterSaleNo;
    private final String salesOrderNo;
    private final String fulfillmentNo;
    private final BigDecimal refundAmount;
    private final String reason;
    private int status;
    private BigDecimal refundedAmount;
    private long version;
    private final List<OmsEvent> events = new ArrayList<>();

    private AfterSaleAggregate(String afterSaleNo, String salesOrderNo, String fulfillmentNo,
                               BigDecimal refundAmount, String reason, int status,
                               BigDecimal refundedAmount, long version) {
        if (blank(afterSaleNo) || blank(salesOrderNo) || blank(fulfillmentNo) || refundAmount == null
                || refundAmount.signum() <= 0 || blank(reason)) {
            throw new IllegalArgumentException("after-sale references, amount and reason are required");
        }
        this.afterSaleNo = afterSaleNo;
        this.salesOrderNo = salesOrderNo;
        this.fulfillmentNo = fulfillmentNo;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = status;
        this.refundedAmount = refundedAmount == null ? BigDecimal.ZERO : refundedAmount;
        this.version = version;
    }

    public static AfterSaleAggregate create(String afterSaleNo, String salesOrderNo, String fulfillmentNo,
                                             BigDecimal refundAmount, String reason) {
        AfterSaleAggregate aggregate = new AfterSaleAggregate(afterSaleNo, salesOrderNo, fulfillmentNo,
                refundAmount, reason, PENDING_REVIEW, BigDecimal.ZERO, 1);
        aggregate.events.add(OmsEvent.of("AfterSaleCreated", afterSaleNo, salesOrderNo));
        return aggregate;
    }

    public static AfterSaleAggregate restore(String afterSaleNo, String salesOrderNo, String fulfillmentNo,
                                              BigDecimal refundAmount, String reason, int status,
                                              BigDecimal refundedAmount, long version) {
        return new AfterSaleAggregate(afterSaleNo, salesOrderNo, fulfillmentNo, refundAmount, reason, status,
                refundedAmount, version);
    }

    public void approve(String remark) {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("after-sale is not pending review");
        }
        if (blank(remark)) {
            throw new IllegalArgumentException("approval remark is required");
        }
        status = APPROVED;
        version++;
        events.add(OmsEvent.of("AfterSaleApproved", afterSaleNo, remark));
    }

    public void requestRefund() {
        if (status != APPROVED) {
            throw new IllegalStateException("after-sale is not approved");
        }
        status = REFUND_REQUESTED;
        version++;
        events.add(OmsEvent.of("RefundRequested", afterSaleNo, refundAmount.toPlainString()));
    }

    public void markRefunded(BigDecimal amount) {
        if (status != REFUND_REQUESTED) {
            throw new IllegalStateException("refund is not requested");
        }
        if (amount == null || amount.signum() <= 0 || amount.compareTo(refundAmount) > 0) {
            throw new IllegalArgumentException("refund amount exceeds request");
        }
        refundedAmount = amount;
        status = REFUNDED;
        version++;
        events.add(OmsEvent.of("RefundCompleted", afterSaleNo, amount.toPlainString()));
    }

    public void complete() {
        if (status != REFUNDED) {
            throw new IllegalStateException("after-sale is not refunded");
        }
        status = COMPLETED;
        version++;
        events.add(OmsEvent.of("AfterSaleCompleted", afterSaleNo, salesOrderNo));
    }

    public List<OmsEvent> pullEvents() {
        List<OmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String afterSaleNo() { return afterSaleNo; }
    public String salesOrderNo() { return salesOrderNo; }
    public String fulfillmentNo() { return fulfillmentNo; }
    public BigDecimal refundAmount() { return refundAmount; }
    public String reason() { return reason; }
    public int status() { return status; }
    public BigDecimal refundedAmount() { return refundedAmount; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
