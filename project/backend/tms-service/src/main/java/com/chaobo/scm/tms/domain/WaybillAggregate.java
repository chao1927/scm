package com.chaobo.scm.tms.domain;

import java.util.ArrayList;
import java.util.List;

public class WaybillAggregate {
    public static final int CREATED = 1;
    public static final int VOIDED = 2;

    private final String waybillNo;
    private final String taskNo;
    private final String carrierCode;
    private final String carrierName;
    private final String carrierWaybillNo;
    private final String logisticsProductCode;
    private final String receiptPayload;
    private int status;
    private String voidReason;
    private String approvalNo;
    private long version;
    private final List<TmsEvent> events = new ArrayList<>();

    private WaybillAggregate(String waybillNo, String taskNo, String carrierCode, String carrierName,
                             String carrierWaybillNo, String logisticsProductCode, String receiptPayload,
                             int status, String voidReason, String approvalNo, long version) {
        if (blank(waybillNo) || blank(taskNo) || blank(carrierCode) || blank(carrierName)
                || blank(carrierWaybillNo) || blank(logisticsProductCode)) {
            throw new IllegalArgumentException("waybill references and carrier receipt are required");
        }
        this.waybillNo = waybillNo;
        this.taskNo = taskNo;
        this.carrierCode = carrierCode;
        this.carrierName = carrierName;
        this.carrierWaybillNo = carrierWaybillNo;
        this.logisticsProductCode = logisticsProductCode;
        this.receiptPayload = receiptPayload;
        this.status = status;
        this.voidReason = voidReason;
        this.approvalNo = approvalNo;
        this.version = version;
    }

    public static WaybillAggregate create(String waybillNo, String taskNo, String carrierCode, String carrierName,
                                          String carrierWaybillNo, String logisticsProductCode,
                                          String receiptPayload) {
        WaybillAggregate aggregate = new WaybillAggregate(waybillNo, taskNo, carrierCode, carrierName,
                carrierWaybillNo, logisticsProductCode, receiptPayload, CREATED, null, null, 1);
        aggregate.events.add(TmsEvent.of("WaybillCreated", waybillNo,
                taskNo + "|" + carrierCode + "|" + carrierWaybillNo));
        return aggregate;
    }

    public static WaybillAggregate restore(String waybillNo, String taskNo, String carrierCode, String carrierName,
                                           String carrierWaybillNo, String logisticsProductCode,
                                           String receiptPayload, int status, String voidReason, String approvalNo,
                                           long version) {
        return new WaybillAggregate(waybillNo, taskNo, carrierCode, carrierName, carrierWaybillNo,
                logisticsProductCode, receiptPayload, status, voidReason, approvalNo, version);
    }

    public void voidWaybill(String reason, String approvalNo, long expectedVersion) {
        if (status != CREATED) {
            throw new IllegalStateException("waybill is not voidable");
        }
        if (version != expectedVersion) {
            throw new IllegalStateException("waybill version conflict");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("void reason is required");
        }
        status = VOIDED;
        voidReason = reason;
        this.approvalNo = approvalNo;
        version++;
        events.add(TmsEvent.of("WaybillVoided", waybillNo, reason));
    }

    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String waybillNo() { return waybillNo; }
    public String taskNo() { return taskNo; }
    public String carrierCode() { return carrierCode; }
    public String carrierName() { return carrierName; }
    public String carrierWaybillNo() { return carrierWaybillNo; }
    public String logisticsProductCode() { return logisticsProductCode; }
    public String receiptPayload() { return receiptPayload; }
    public int status() { return status; }
    public String voidReason() { return voidReason; }
    public String approvalNo() { return approvalNo; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
