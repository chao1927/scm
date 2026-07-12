package com.chaobo.scm.purchase.application.operations;

import java.time.OffsetDateTime;

public final class PurchaseOperationsViews {
    private PurchaseOperationsViews() {
    }

    public record FailedEvent(long id, String sourceSystem, String eventCode, String eventType,
                              String consumerName, int retryCount, String reason,
                              OffsetDateTime updatedAt) {
    }
}
