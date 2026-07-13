package com.chaobo.scm.tms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LogisticsFeeSourceAggregate {
    public static final int PENDING_PUSH = 1;
    public static final int PUSHED = 2;
    public static final int PUSH_FAILED = 3;

    private final String feeSourceNo;
    private final String waybillNo;
    private final String carrierCode;
    private final String logisticsProductCode;
    private final String feeItemCode;
    private final BigDecimal amount;
    private final String currency;
    private final String billingPeriod;
    private final String responsibleParty;
    private int pushStatus;
    private String bmsReceiveNo;
    private String failureReason;
    private long version;
    private final List<TmsEvent> events = new ArrayList<>();

    private LogisticsFeeSourceAggregate(String feeSourceNo, String waybillNo, String carrierCode,
                                        String logisticsProductCode, String feeItemCode, BigDecimal amount,
                                        String currency, String billingPeriod, String responsibleParty,
                                        int pushStatus, String bmsReceiveNo, String failureReason, long version) {
        if (blank(feeSourceNo) || blank(waybillNo) || blank(carrierCode) || blank(logisticsProductCode)
                || blank(feeItemCode) || blank(currency) || blank(billingPeriod) || blank(responsibleParty)) {
            throw new IllegalArgumentException("fee source references are required");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("fee source amount cannot be negative");
        }
        this.feeSourceNo = feeSourceNo;
        this.waybillNo = waybillNo;
        this.carrierCode = carrierCode;
        this.logisticsProductCode = logisticsProductCode;
        this.feeItemCode = feeItemCode;
        this.amount = amount;
        this.currency = currency;
        this.billingPeriod = billingPeriod;
        this.responsibleParty = responsibleParty;
        this.pushStatus = pushStatus;
        this.bmsReceiveNo = bmsReceiveNo;
        this.failureReason = failureReason;
        this.version = version;
    }

    public static LogisticsFeeSourceAggregate generate(String feeSourceNo, String waybillNo, String carrierCode,
                                                       String logisticsProductCode, String feeItemCode,
                                                       BigDecimal amount, String currency, String billingPeriod,
                                                       String responsibleParty) {
        LogisticsFeeSourceAggregate aggregate = new LogisticsFeeSourceAggregate(feeSourceNo, waybillNo, carrierCode,
                logisticsProductCode, feeItemCode, amount, currency, billingPeriod, responsibleParty, PENDING_PUSH,
                null, null, 1);
        aggregate.events.add(TmsEvent.of("LogisticsFeeSourceGenerated", feeSourceNo,
                waybillNo + "|" + feeItemCode + "|" + amount.toPlainString()));
        return aggregate;
    }

    public static LogisticsFeeSourceAggregate restore(String feeSourceNo, String waybillNo, String carrierCode,
                                                       String logisticsProductCode, String feeItemCode,
                                                       BigDecimal amount, String currency, String billingPeriod,
                                                       String responsibleParty, int pushStatus, String bmsReceiveNo,
                                                       String failureReason, long version) {
        return new LogisticsFeeSourceAggregate(feeSourceNo, waybillNo, carrierCode, logisticsProductCode,
                feeItemCode, amount, currency, billingPeriod, responsibleParty, pushStatus, bmsReceiveNo,
                failureReason, version);
    }

    public void pushToBms(String bmsReceiveNo) {
        if (pushStatus == PUSHED) {
            return;
        }
        if (blank(bmsReceiveNo)) {
            throw new IllegalArgumentException("BMS receive number is required");
        }
        pushStatus = PUSHED;
        this.bmsReceiveNo = bmsReceiveNo;
        failureReason = null;
        version++;
        events.add(TmsEvent.of("LogisticsFeeSourcePushed", feeSourceNo, bmsReceiveNo));
    }

    public void markPushFailed(String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("push failure reason is required");
        }
        pushStatus = PUSH_FAILED;
        failureReason = reason;
        version++;
        events.add(TmsEvent.of("LogisticsFeeSourcePushFailed", feeSourceNo, reason));
    }

    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String feeSourceNo() { return feeSourceNo; }
    public String waybillNo() { return waybillNo; }
    public String carrierCode() { return carrierCode; }
    public String logisticsProductCode() { return logisticsProductCode; }
    public String feeItemCode() { return feeItemCode; }
    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    public String billingPeriod() { return billingPeriod; }
    public String responsibleParty() { return responsibleParty; }
    public int pushStatus() { return pushStatus; }
    public String bmsReceiveNo() { return bmsReceiveNo; }
    public String failureReason() { return failureReason; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
