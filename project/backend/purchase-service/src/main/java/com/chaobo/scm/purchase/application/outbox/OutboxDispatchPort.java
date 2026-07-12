package com.chaobo.scm.purchase.application.outbox;

import java.util.List;

public interface OutboxDispatchPort {
    List<OutboxMessage> claim(int batchSize, int maxRetries);

    void markPublished(long eventId);

    void markFailed(long eventId, String reason);
}
