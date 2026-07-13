package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

public class MasterDataRecordAggregate {
    public static final int DRAFT = 1;
    public static final int PENDING_REVIEW = 2;
    public static final int ENABLED = 3;
    public static final int REJECTED = 4;
    public static final int FROZEN = 5;
    public static final int DISABLED = 6;

    private final String recordNo;
    private final String typeCode;
    private final String dataCode;
    private String dataName;
    private String dataPayload;
    private int status;
    private int currentVersionNo;
    private String reason;
    private long version;
    private final List<MdmEvent> events = new ArrayList<>();

    private MasterDataRecordAggregate(String recordNo, String typeCode, String dataCode, String dataName,
                                      String dataPayload, int status, int currentVersionNo, String reason,
                                      long version) {
        if (blank(recordNo) || blank(typeCode) || blank(dataCode) || blank(dataName) || blank(dataPayload)) {
            throw new IllegalArgumentException("master data record references and payload are required");
        }
        this.recordNo = recordNo;
        this.typeCode = typeCode;
        this.dataCode = dataCode;
        this.dataName = dataName;
        this.dataPayload = dataPayload;
        this.status = status;
        this.currentVersionNo = currentVersionNo;
        this.reason = reason;
        this.version = version;
    }

    public static MasterDataRecordAggregate create(String recordNo, String typeCode, String dataCode,
                                                   String dataName, String dataPayload) {
        MasterDataRecordAggregate aggregate = new MasterDataRecordAggregate(recordNo, typeCode, dataCode,
                dataName, dataPayload, DRAFT, 0, null, 1);
        aggregate.events.add(MdmEvent.of("MasterDataDraftCreated", recordNo, typeCode + "|" + dataCode));
        return aggregate;
    }

    public static MasterDataRecordAggregate restore(String recordNo, String typeCode, String dataCode,
                                                    String dataName, String dataPayload, int status,
                                                    int currentVersionNo, String reason, long version) {
        return new MasterDataRecordAggregate(recordNo, typeCode, dataCode, dataName, dataPayload, status,
                currentVersionNo, reason, version);
    }

    public void change(String dataName, String dataPayload, String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != DRAFT && status != REJECTED) {
            throw new IllegalStateException("master data record is not editable");
        }
        if (blank(dataName) || blank(dataPayload)) {
            throw new IllegalArgumentException("data name and payload are required");
        }
        this.dataName = dataName;
        this.dataPayload = dataPayload;
        this.reason = reason;
        status = DRAFT;
        version++;
        events.add(MdmEvent.of("MasterDataChanged", recordNo, reason == null ? "" : reason));
    }

    public void submitReview(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != DRAFT && status != REJECTED) {
            throw new IllegalStateException("master data record cannot submit review");
        }
        status = PENDING_REVIEW;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataSubmitted", recordNo, reason == null ? "" : reason));
    }

    public void approve(String remark, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("master data record is not pending review");
        }
        status = ENABLED;
        currentVersionNo++;
        reason = remark;
        version++;
        events.add(MdmEvent.of("MasterDataEnabled", recordNo, typeCode + "|" + dataCode + "|" + currentVersionNo));
    }

    public void reject(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("master data record is not pending review");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("reject reason is required");
        }
        status = REJECTED;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataRejected", recordNo, reason));
    }

    public void freeze(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != ENABLED) {
            throw new IllegalStateException("master data record is not enabled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("freeze reason is required");
        }
        status = FROZEN;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataFrozen", recordNo, reason));
    }

    public void disable(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != ENABLED && status != FROZEN) {
            throw new IllegalStateException("master data record cannot be disabled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("disable reason is required");
        }
        status = DISABLED;
        this.reason = reason;
        version++;
        events.add(MdmEvent.of("MasterDataDisabled", recordNo, reason));
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String recordNo() { return recordNo; }
    public String typeCode() { return typeCode; }
    public String dataCode() { return dataCode; }
    public String dataName() { return dataName; }
    public String dataPayload() { return dataPayload; }
    public int status() { return status; }
    public int currentVersionNo() { return currentVersionNo; }
    public String reason() { return reason; }
    public long version() { return version; }

    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("master data record version conflict");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
