package com.chaobo.scm.supplier.domain.shared;

import java.time.OffsetDateTime;
import java.util.Map;

public record DomainEvent(
        long eventId,
        String eventCode,
        String eventType,
        String eventName,
        String aggregateType,
        long aggregateId,
        String aggregateNo,
        int aggregateVersion,
        long operatorId,
        OffsetDateTime occurredAt,
        Map<String, Object> payload) {

    public DomainEvent {
        payload = Map.copyOf(payload);
    }
}
