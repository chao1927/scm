package com.chaobo.scm.tms.domain;

import java.time.LocalDateTime;

public record TmsEvent(String eventType, String businessNo, String payload, LocalDateTime occurredAt) {
    public static TmsEvent of(String eventType, String businessNo, String payload) {
        return new TmsEvent(eventType, businessNo, payload, LocalDateTime.now());
    }
}
