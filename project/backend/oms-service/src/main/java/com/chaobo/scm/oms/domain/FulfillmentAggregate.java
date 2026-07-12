package com.chaobo.scm.oms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FulfillmentAggregate {
    public static final int PENDING_RESERVATION = 1;
    public static final int RESERVED = 2;
    public static final int PENDING_OUTBOUND = 3;
    public static final int OUTBOUND_ISSUED = 4;
    public static final int SHIPPED = 5;
    public static final int CANCELLED = 6;
    public static final int FAILED = 7;

    private final String fulfillmentNo;
    private final String salesOrderNo;
    private final String channelCode;
    private final Long customerId;
    private Long warehouseId;
    private String warehouseCode;
    private final String logisticsProductCode;
    private final List<Line> lines;
    private int status;
    private String reservationRefNo;
    private String reservationNo;
    private String outboundNo;
    private String failureReason;
    private String splitReason;
    private long version;
    private final List<OmsEvent> events = new ArrayList<>();

    private FulfillmentAggregate(String fulfillmentNo, String salesOrderNo, String channelCode, Long customerId,
                                 Long warehouseId, String warehouseCode, String logisticsProductCode,
                                 List<Line> lines, int status, String reservationRefNo, String reservationNo,
                                 String outboundNo, String failureReason, String splitReason, long version) {
        if (blank(fulfillmentNo) || blank(salesOrderNo) || blank(channelCode) || customerId == null
                || warehouseId == null || warehouseId <= 0 || blank(warehouseCode)) {
            throw new IllegalArgumentException("fulfillment references and warehouse are required");
        }
        validateLines(lines);
        this.fulfillmentNo = fulfillmentNo;
        this.salesOrderNo = salesOrderNo;
        this.channelCode = channelCode;
        this.customerId = customerId;
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
        this.logisticsProductCode = logisticsProductCode;
        this.lines = new ArrayList<>(lines);
        this.status = status;
        this.reservationRefNo = reservationRefNo;
        this.reservationNo = reservationNo;
        this.outboundNo = outboundNo;
        this.failureReason = failureReason;
        this.splitReason = splitReason;
        this.version = version;
    }

    public static FulfillmentAggregate create(String fulfillmentNo, String salesOrderNo, String channelCode,
                                              Long customerId, Long warehouseId, String warehouseCode,
                                              String logisticsProductCode, List<Line> lines) {
        FulfillmentAggregate aggregate = new FulfillmentAggregate(fulfillmentNo, salesOrderNo, channelCode, customerId,
                warehouseId, warehouseCode, logisticsProductCode, lines, PENDING_RESERVATION, null, null, null, null,
                null, 1);
        aggregate.events.add(OmsEvent.of("FulfillmentOrderCreated", fulfillmentNo,
                salesOrderNo + "|" + warehouseCode));
        aggregate.events.add(OmsEvent.of("FulfillmentWarehouseAllocated", fulfillmentNo, warehouseCode));
        return aggregate;
    }

    public static FulfillmentAggregate restore(String fulfillmentNo, String salesOrderNo, String channelCode,
                                                Long customerId, Long warehouseId, String warehouseCode,
                                                String logisticsProductCode, List<Line> lines, int status,
                                                String reservationRefNo, String reservationNo, String outboundNo,
                                                String failureReason, String splitReason, long version) {
        return new FulfillmentAggregate(fulfillmentNo, salesOrderNo, channelCode, customerId, warehouseId,
                warehouseCode, logisticsProductCode, lines, status, reservationRefNo, reservationNo, outboundNo,
                failureReason, splitReason, version);
    }

    public void changeWarehouse(Long targetWarehouseId, String targetWarehouseCode, String reason) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (!blank(reservationRefNo)) {
            throw new IllegalStateException("fulfillment is not pending reservation because reservation is already requested");
        }
        if (targetWarehouseId == null || targetWarehouseId <= 0 || blank(targetWarehouseCode)) {
            throw new IllegalArgumentException("target warehouse is required");
        }
        if (targetWarehouseId.equals(warehouseId)) {
            throw new IllegalArgumentException("target warehouse must be different");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("warehouse change reason is required");
        }
        warehouseId = targetWarehouseId;
        warehouseCode = targetWarehouseCode;
        version++;
        events.add(OmsEvent.of("FulfillmentWarehouseChanged", fulfillmentNo,
                targetWarehouseCode + "|" + reason));
    }

    public FulfillmentAggregate split(String childFulfillmentNo, List<Line> childLines, String reason) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (!blank(reservationRefNo)) {
            throw new IllegalStateException("fulfillment is not pending reservation because reservation is already requested");
        }
        if (blank(childFulfillmentNo) || blank(reason)) {
            throw new IllegalArgumentException("child fulfillment number and split reason are required");
        }
        validateLines(childLines);
        List<Line> remaining = new ArrayList<>();
        for (Line original : lines) {
            Line child = findLine(childLines, original.skuCode());
            BigDecimal childQty = child == null ? BigDecimal.ZERO : child.quantity();
            if (childQty.compareTo(original.quantity()) > 0) {
                throw new IllegalArgumentException("split quantity exceeds fulfillment quantity");
            }
            BigDecimal remainingQty = original.quantity().subtract(childQty);
            if (remainingQty.signum() > 0) {
                remaining.add(new Line(original.skuCode(), remainingQty));
            }
        }
        if (remaining.isEmpty()) {
            throw new IllegalArgumentException("parent fulfillment must retain a line");
        }
        if (childLines.stream().anyMatch(line -> findLine(lines, line.skuCode()) == null)) {
            throw new IllegalArgumentException("split SKU is not in parent fulfillment");
        }
        lines.clear();
        lines.addAll(remaining);
        version++;
        events.add(OmsEvent.of("FulfillmentSplit", fulfillmentNo, childFulfillmentNo + "|" + reason));
        return new FulfillmentAggregate(childFulfillmentNo, salesOrderNo, channelCode, customerId, warehouseId,
                warehouseCode, logisticsProductCode, childLines, PENDING_RESERVATION, null, null, null, null, reason, 1);
    }

    public void requestReservation(String reservationRefNo) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (blank(reservationRefNo)) {
            throw new IllegalArgumentException("reservation reference is required");
        }
        this.reservationRefNo = reservationRefNo;
        version++;
        events.add(OmsEvent.of("StockReservationRequested", fulfillmentNo,
                reservationRefNo + "|" + totalQuantity().toPlainString()));
    }

    public void recordReservationSuccess(String reservationNo, BigDecimal reservedQty) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (blank(reservationRefNo)) {
            throw new IllegalStateException("reservation was not requested");
        }
        if (blank(reservationNo) || reservedQty == null || reservedQty.signum() <= 0
                || reservedQty.compareTo(totalQuantity()) != 0) {
            throw new IllegalArgumentException("reservation quantity must equal fulfillment quantity");
        }
        this.reservationNo = reservationNo;
        status = RESERVED;
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            lines.set(i, new Line(line.skuCode(), line.quantity(), line.quantity(), line.shippedQty()));
        }
        version++;
        events.add(OmsEvent.of("FulfillmentInventoryReserved", fulfillmentNo,
                reservationNo + "|" + reservedQty.toPlainString()));
    }

    public void recordReservationFailure(String reason) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (blank(reservationRefNo)) {
            throw new IllegalStateException("reservation was not requested");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("reservation failure reason is required");
        }
        status = FAILED;
        failureReason = reason;
        version++;
        events.add(OmsEvent.of("FulfillmentReservationFailed", fulfillmentNo, reason));
    }

    public void markOutboundIssued(String outboundNo) {
        ensureStatus(RESERVED, "reservation");
        if (blank(outboundNo)) {
            throw new IllegalArgumentException("outbound number is required");
        }
        this.outboundNo = outboundNo;
        status = OUTBOUND_ISSUED;
        version++;
        events.add(OmsEvent.of("OutboundInstructionIssued", fulfillmentNo, outboundNo));
    }

    public void markWmsShipped() {
        if (status != OUTBOUND_ISSUED) {
            throw new IllegalStateException("fulfillment is not outbound issued");
        }
        status = SHIPPED;
        version++;
        events.add(OmsEvent.of("FulfillmentShipped", fulfillmentNo, outboundNo));
    }

    public void markCancelled(String reason) {
        if (status == SHIPPED) {
            throw new IllegalStateException("shipped fulfillment cannot be cancelled");
        }
        if (status == CANCELLED) {
            return;
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("cancel reason is required");
        }
        status = CANCELLED;
        version++;
        events.add(OmsEvent.of("FulfillmentOrderCanceled", fulfillmentNo, reason));
    }

    public List<OmsEvent> pullEvents() {
        List<OmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String fulfillmentNo() { return fulfillmentNo; }
    public String salesOrderNo() { return salesOrderNo; }
    public String channelCode() { return channelCode; }
    public Long customerId() { return customerId; }
    public Long warehouseId() { return warehouseId; }
    public String warehouseCode() { return warehouseCode; }
    public String logisticsProductCode() { return logisticsProductCode; }
    public List<Line> lines() { return List.copyOf(lines); }
    public int status() { return status; }
    public String reservationRefNo() { return reservationRefNo; }
    public String reservationNo() { return reservationNo; }
    public String outboundNo() { return outboundNo; }
    public String failureReason() { return failureReason; }
    public String splitReason() { return splitReason; }
    public long version() { return version; }

    private BigDecimal totalQuantity() {
        return lines.stream().map(Line::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void ensureStatus(int expected, String label) {
        if (status != expected) {
            throw new IllegalStateException("fulfillment is not " + label);
        }
    }

    private static Line findLine(List<Line> source, String skuCode) {
        return source.stream().filter(line -> line.skuCode().equals(skuCode)).findFirst().orElse(null);
    }

    private static void validateLines(List<Line> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("fulfillment lines are required");
        }
        for (Line line : lines) {
            if (line == null || blank(line.skuCode()) || line.quantity() == null || line.quantity().signum() <= 0) {
                throw new IllegalArgumentException("invalid fulfillment line");
            }
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record Line(String skuCode, BigDecimal quantity, BigDecimal reservedQty, BigDecimal shippedQty) {
        public Line(String skuCode, BigDecimal quantity) {
            this(skuCode, quantity, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        public Line {
            if (reservedQty == null || shippedQty == null) {
                throw new IllegalArgumentException("line quantities cannot be null");
            }
        }
    }
}
