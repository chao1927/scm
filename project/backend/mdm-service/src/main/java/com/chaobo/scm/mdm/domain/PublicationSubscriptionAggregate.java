package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

public class PublicationSubscriptionAggregate {
    public static final int ENABLED = 1;
    public static final int DISABLED = 2;

    private final String subscriptionNo;
    private final String typeCode;
    private final String targetSystem;
    private final String eventTopic;
    private final String filterRule;
    private int status;
    private long version;
    private final List<MdmEvent> events = new ArrayList<>();

    private PublicationSubscriptionAggregate(String subscriptionNo, String typeCode, String targetSystem,
                                             String eventTopic, String filterRule, int status, long version) {
        if (blank(subscriptionNo) || blank(typeCode) || blank(targetSystem) || blank(eventTopic)) {
            throw new IllegalArgumentException("publication subscription references are required");
        }
        this.subscriptionNo = subscriptionNo;
        this.typeCode = typeCode;
        this.targetSystem = targetSystem;
        this.eventTopic = eventTopic;
        this.filterRule = filterRule;
        this.status = status;
        this.version = version;
    }

    public static PublicationSubscriptionAggregate create(String subscriptionNo, String typeCode, String targetSystem,
                                                         String eventTopic, String filterRule) {
        PublicationSubscriptionAggregate aggregate = new PublicationSubscriptionAggregate(subscriptionNo, typeCode,
                targetSystem, eventTopic, filterRule, ENABLED, 1);
        aggregate.events.add(MdmEvent.of("PublicationSubscriptionCreated", subscriptionNo,
                typeCode + "|" + targetSystem + "|" + eventTopic));
        return aggregate;
    }

    public static PublicationSubscriptionAggregate restore(String subscriptionNo, String typeCode, String targetSystem,
                                                          String eventTopic, String filterRule, int status,
                                                          long version) {
        return new PublicationSubscriptionAggregate(subscriptionNo, typeCode, targetSystem, eventTopic, filterRule,
                status, version);
    }

    public void disable(String reason, long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("publication subscription version conflict");
        }
        if (status == DISABLED) {
            return;
        }
        status = DISABLED;
        version++;
        events.add(MdmEvent.of("PublicationSubscriptionDisabled", subscriptionNo, reason == null ? "" : reason));
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String subscriptionNo() { return subscriptionNo; }
    public String typeCode() { return typeCode; }
    public String targetSystem() { return targetSystem; }
    public String eventTopic() { return eventTopic; }
    public String filterRule() { return filterRule; }
    public int status() { return status; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
