package com.chaobo.scm.purchase.domain.shared;

import java.time.OffsetDateTime;
import java.util.Map;

public record DomainEvent(
        long eventId,
        String eventCode,
        String eventType,
        String aggregateType,
        String aggregateId,
        int aggregateVersion,
        OffsetDateTime occurredAt,
        Map<String, Object> payload) {
}
