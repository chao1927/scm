package com.chaobo.scm.tms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrackingAggregate {
    private final String trackNo;
    private final String waybillNo;
    private final String nodeCode;
    private final String description;
    private final String location;
    private final LocalDateTime trackAt;
    private final String sourceType;
    private final String rawEventId;
    private final String manualReason;
    private final List<TmsEvent> events = new ArrayList<>();

    private TrackingAggregate(String trackNo, String waybillNo, String nodeCode, String description, String location,
                              LocalDateTime trackAt, String sourceType, String rawEventId, String manualReason) {
        if (blank(trackNo) || blank(waybillNo) || blank(nodeCode) || blank(description) || trackAt == null
                || blank(sourceType)) {
            throw new IllegalArgumentException("tracking node references are required");
        }
        this.trackNo = trackNo;
        this.waybillNo = waybillNo;
        this.nodeCode = nodeCode;
        this.description = description;
        this.location = location;
        this.trackAt = trackAt;
        this.sourceType = sourceType;
        this.rawEventId = rawEventId;
        this.manualReason = manualReason;
    }

    public static TrackingAggregate append(String trackNo, String waybillNo, String nodeCode, String description,
                                           String location, LocalDateTime trackAt, String sourceType,
                                           String rawEventId) {
        TrackingAggregate aggregate = new TrackingAggregate(trackNo, waybillNo, nodeCode, description, location,
                trackAt, sourceType, rawEventId, null);
        aggregate.events.add(TmsEvent.of("TrackingAppended", trackNo, waybillNo + "|" + nodeCode));
        if ("ARRIVED".equals(nodeCode)) {
            aggregate.events.add(TmsEvent.of("TransportArrived", waybillNo, location == null ? "" : location));
        }
        return aggregate;
    }

    public static TrackingAggregate supplement(String trackNo, String waybillNo, String nodeCode, String description,
                                               String location, LocalDateTime trackAt, String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("supplement reason is required");
        }
        TrackingAggregate aggregate = new TrackingAggregate(trackNo, waybillNo, nodeCode, description, location,
                trackAt, "MANUAL", null, reason);
        aggregate.events.add(TmsEvent.of("TrackingSupplemented", trackNo, waybillNo + "|" + nodeCode + "|" + reason));
        return aggregate;
    }

    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String trackNo() { return trackNo; }
    public String waybillNo() { return waybillNo; }
    public String nodeCode() { return nodeCode; }
    public String description() { return description; }
    public String location() { return location; }
    public LocalDateTime trackAt() { return trackAt; }
    public String sourceType() { return sourceType; }
    public String rawEventId() { return rawEventId; }
    public String manualReason() { return manualReason; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
