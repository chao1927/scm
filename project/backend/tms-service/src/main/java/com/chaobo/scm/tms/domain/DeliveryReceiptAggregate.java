package com.chaobo.scm.tms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeliveryReceiptAggregate {
    public static final int SIGNED = 1;
    public static final int REJECTED = 2;
    public static final int PARTIAL_SIGNED = 3;

    private final String receiptNo;
    private final String waybillNo;
    private final int result;
    private final String signedBy;
    private final LocalDateTime signedAt;
    private final String rejectReason;
    private final String proofUrl;
    private final List<TmsEvent> events = new ArrayList<>();

    private DeliveryReceiptAggregate(String receiptNo, String waybillNo, int result, String signedBy,
                                     LocalDateTime signedAt, String rejectReason, String proofUrl) {
        if (blank(receiptNo) || blank(waybillNo) || signedAt == null) {
            throw new IllegalArgumentException("delivery receipt references are required");
        }
        if (result == SIGNED && blank(signedBy)) {
            throw new IllegalArgumentException("signed by is required");
        }
        if (result == REJECTED && blank(rejectReason)) {
            throw new IllegalArgumentException("reject reason is required");
        }
        if (!List.of(SIGNED, REJECTED, PARTIAL_SIGNED).contains(result)) {
            throw new IllegalArgumentException("unsupported receipt result");
        }
        this.receiptNo = receiptNo;
        this.waybillNo = waybillNo;
        this.result = result;
        this.signedBy = signedBy;
        this.signedAt = signedAt;
        this.rejectReason = rejectReason;
        this.proofUrl = proofUrl;
    }

    public static DeliveryReceiptAggregate record(String receiptNo, String waybillNo, int result, String signedBy,
                                                  LocalDateTime signedAt, String rejectReason, String proofUrl) {
        DeliveryReceiptAggregate aggregate = new DeliveryReceiptAggregate(receiptNo, waybillNo, result, signedBy,
                signedAt, rejectReason, proofUrl);
        String eventType = switch (result) {
            case SIGNED -> "TransportSigned";
            case REJECTED -> "TransportRejected";
            case PARTIAL_SIGNED -> "PartialSigned";
            default -> throw new IllegalArgumentException("unsupported receipt result");
        };
        aggregate.events.add(TmsEvent.of(eventType, waybillNo, receiptNo));
        return aggregate;
    }

    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String receiptNo() { return receiptNo; }
    public String waybillNo() { return waybillNo; }
    public int result() { return result; }
    public String signedBy() { return signedBy; }
    public LocalDateTime signedAt() { return signedAt; }
    public String rejectReason() { return rejectReason; }
    public String proofUrl() { return proofUrl; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
