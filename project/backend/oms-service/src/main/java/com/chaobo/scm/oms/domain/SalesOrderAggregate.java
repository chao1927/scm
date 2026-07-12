package com.chaobo.scm.oms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderAggregate {
    public static final int PENDING_REVIEW = 1;
    public static final int APPROVED = 2;
    public static final int INTERCEPTED = 3;

    private final String orderNo;
    private final String channelCode;
    private final String channelOrderNo;
    private final Long customerId;
    private final String receiverAddress;
    private final List<OrderLine> lines;
    private BigDecimal totalAmount;
    private int status;
    private String reviewRemark;
    private long version;
    private final List<OmsEvent> events = new ArrayList<>();

    private SalesOrderAggregate(String orderNo, String channelCode, String channelOrderNo, Long customerId,
                                String receiverAddress, List<OrderLine> lines, BigDecimal totalAmount,
                                int status, String reviewRemark, long version) {
        if (blank(orderNo) || blank(channelCode) || blank(channelOrderNo) || customerId == null || blank(receiverAddress)) {
            throw new IllegalArgumentException("orderNo, channel, customer and address are required");
        }
        validateLines(lines);
        this.orderNo = orderNo;
        this.channelCode = channelCode;
        this.channelOrderNo = channelOrderNo;
        this.customerId = customerId;
        this.receiverAddress = receiverAddress;
        this.lines = new ArrayList<>(lines);
        this.totalAmount = totalAmount == null ? amountOf(lines) : totalAmount;
        if (this.totalAmount.signum() < 0) {
            throw new IllegalArgumentException("total amount cannot be negative");
        }
        this.status = status;
        this.reviewRemark = reviewRemark;
        this.version = version;
    }

    public static SalesOrderAggregate create(String orderNo, String channelCode, String channelOrderNo, Long customerId,
                                             String receiverAddress, List<OrderLine> lines) {
        SalesOrderAggregate aggregate = new SalesOrderAggregate(orderNo, channelCode, channelOrderNo, customerId,
                receiverAddress, lines, amountOf(lines), PENDING_REVIEW, null, 1);
        aggregate.events.add(OmsEvent.of("ChannelOrderReceived", orderNo, channelCode + ":" + channelOrderNo));
        aggregate.events.add(OmsEvent.of("SalesOrderCreated", orderNo, aggregate.totalAmount.toPlainString()));
        return aggregate;
    }

    public static SalesOrderAggregate restore(String orderNo, String channelCode, String channelOrderNo, Long customerId,
                                              String receiverAddress, List<OrderLine> lines, BigDecimal totalAmount,
                                              int status, String reviewRemark, long version) {
        return new SalesOrderAggregate(orderNo, channelCode, channelOrderNo, customerId, receiverAddress, lines, totalAmount, status, reviewRemark, version);
    }

    public void approve(String remark) {
        ensurePendingReview();
        status = APPROVED;
        reviewRemark = remark;
        version++;
        events.add(OmsEvent.of("SalesOrderReviewed", orderNo, "APPROVED"));
    }

    public void intercept(String reason) {
        ensurePendingReview();
        if (blank(reason)) {
            throw new IllegalArgumentException("intercept reason is required");
        }
        status = INTERCEPTED;
        reviewRemark = reason;
        version++;
        events.add(OmsEvent.of("SalesOrderIntercepted", orderNo, reason));
    }

    public List<OmsEvent> pullEvents() {
        List<OmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String orderNo() { return orderNo; }
    public String channelCode() { return channelCode; }
    public String channelOrderNo() { return channelOrderNo; }
    public Long customerId() { return customerId; }
    public String receiverAddress() { return receiverAddress; }
    public List<OrderLine> lines() { return List.copyOf(lines); }
    public BigDecimal totalAmount() { return totalAmount; }
    public int status() { return status; }
    public String reviewRemark() { return reviewRemark; }
    public long version() { return version; }

    private void ensurePendingReview() {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("sales order is not pending review");
        }
    }

    private static void validateLines(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("order lines are required");
        }
        for (OrderLine line : lines) {
            if (blank(line.skuCode()) || line.quantity() <= 0 || line.unitPrice() == null || line.unitPrice().signum() < 0) {
                throw new IllegalArgumentException("invalid order line");
            }
        }
    }

    private static BigDecimal amountOf(List<OrderLine> lines) {
        return lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record OrderLine(String skuCode, int quantity, BigDecimal unitPrice) {}
}
