package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

public class PublicationAggregate {
    public static final int PENDING = 1;
    public static final int CONFIRMED = 2;
    public static final int FAILED = 3;

    private final String publicationNo;
    private final String versionNo;
    private final String typeCode;
    private final String dataCode;
    private final String targetSystem;
    private final String eventTopic;
    private int status;
    private int retryCount;
    private String failureReason;
    private long version;
    private final List<MdmEvent> events = new ArrayList<>();

    private PublicationAggregate(String publicationNo, String versionNo, String typeCode, String dataCode,
                                 String targetSystem, String eventTopic, int status, int retryCount,
                                 String failureReason, long version) {
        if (blank(publicationNo) || blank(versionNo) || blank(typeCode) || blank(dataCode)
                || blank(targetSystem) || blank(eventTopic)) {
            throw new IllegalArgumentException("publication references are required");
        }
        this.publicationNo = publicationNo;
        this.versionNo = versionNo;
        this.typeCode = typeCode;
        this.dataCode = dataCode;
        this.targetSystem = targetSystem;
        this.eventTopic = eventTopic;
        this.status = status;
        this.retryCount = retryCount;
        this.failureReason = failureReason;
        this.version = version;
    }

    public static PublicationAggregate create(String publicationNo, String versionNo, String typeCode,
                                              String dataCode, String targetSystem, String eventTopic) {
        PublicationAggregate aggregate = new PublicationAggregate(publicationNo, versionNo, typeCode, dataCode,
                targetSystem, eventTopic, PENDING, 0, null, 1);
        aggregate.events.add(MdmEvent.of("MasterDataPublished", publicationNo,
                typeCode + "|" + dataCode + "|" + targetSystem));
        return aggregate;
    }

    public static PublicationAggregate restore(String publicationNo, String versionNo, String typeCode,
                                               String dataCode, String targetSystem, String eventTopic, int status,
                                               int retryCount, String failureReason, long version) {
        return new PublicationAggregate(publicationNo, versionNo, typeCode, dataCode, targetSystem, eventTopic,
                status, retryCount, failureReason, version);
    }

    public void confirm() {
        if (status == CONFIRMED) {
            return;
        }
        status = CONFIRMED;
        failureReason = null;
        version++;
        events.add(MdmEvent.of("MasterDataPublishConfirmed", publicationNo, targetSystem));
    }

    public void fail(String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("publication failure reason is required");
        }
        status = FAILED;
        failureReason = reason;
        version++;
    }

    public void retry(String reason) {
        if (status != FAILED) {
            throw new IllegalStateException("publication is not failed");
        }
        status = PENDING;
        retryCount++;
        failureReason = null;
        version++;
        events.add(MdmEvent.of("MasterDataRepublished", publicationNo, reason == null ? "" : reason));
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String publicationNo() { return publicationNo; }
    public String versionNo() { return versionNo; }
    public String typeCode() { return typeCode; }
    public String dataCode() { return dataCode; }
    public String targetSystem() { return targetSystem; }
    public String eventTopic() { return eventTopic; }
    public int status() { return status; }
    public int retryCount() { return retryCount; }
    public String failureReason() { return failureReason; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
