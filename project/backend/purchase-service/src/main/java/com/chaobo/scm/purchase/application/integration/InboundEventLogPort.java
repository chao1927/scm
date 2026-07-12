package com.chaobo.scm.purchase.application.integration;

import java.util.Optional;

public interface InboundEventLogPort {
    enum ClaimResult {
        CLAIMED,
        ALREADY_SUCCEEDED,
        IN_PROGRESS
    }

    ClaimResult claim(String sourceSystem, String eventCode, String eventType,
                      String consumerName, String idempotentKey);

    void savePayload(String sourceSystem, String eventCode, String consumerName, String payloadJson);

    void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored);

    void recordFailure(String sourceSystem, String eventCode, String eventType,
                       String consumerName, String idempotentKey, String reason);

    Optional<ReplayEvent> findForReplay(long consumeLogId);

    void markReplayRequested(long consumeLogId, long operatorId, String reason);

    record ReplayEvent(long id, String sourceSystem, String eventCode, String eventType,
                       String consumerName, String payloadJson, int status) {
    }
}
