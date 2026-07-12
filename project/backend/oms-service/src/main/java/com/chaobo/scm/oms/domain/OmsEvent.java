package com.chaobo.scm.oms.domain;

import java.time.LocalDateTime;

public record OmsEvent(String eventType, String businessNo, String payload, LocalDateTime occurredAt) {
    public static OmsEvent of(String eventType, String businessNo, String payload) {
        return new OmsEvent(eventType, businessNo, payload, LocalDateTime.now());
    }
}
