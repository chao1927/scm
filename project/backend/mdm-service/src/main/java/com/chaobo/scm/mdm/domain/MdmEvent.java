package com.chaobo.scm.mdm.domain;

import java.time.LocalDateTime;

public record MdmEvent(String eventType, String businessNo, String payload, LocalDateTime occurredAt) {
    public static MdmEvent of(String eventType, String businessNo, String payload) {
        return new MdmEvent(eventType, businessNo, payload, LocalDateTime.now());
    }
}
