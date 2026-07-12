package com.chaobo.scm.purchase.application.outbox;

public record OutboxMessage(long eventId, String eventCode, String eventType, String aggregateType,
                            String aggregateId, String payloadJson, int retryCount) {
}
