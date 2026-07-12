package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.application.outbox.OutboxDispatchPort;
import com.chaobo.scm.purchase.application.outbox.OutboxMessage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MyBatisOutboxDispatchAdapter implements OutboxDispatchPort {
    private final EventPersistenceMapper mapper;

    public MyBatisOutboxDispatchAdapter(EventPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<OutboxMessage> claim(int batchSize, int maxRetries) {
        var messages = mapper.claimOutbox(batchSize, maxRetries);
        if (!messages.isEmpty()) {
            mapper.markOutboxPublishing(messages.stream().map(OutboxMessage::eventId).toList());
        }
        return messages;
    }

    @Override
    public void markPublished(long eventId) {
        mapper.markOutboxPublished(eventId);
    }

    @Override
    public void markFailed(long eventId, String reason) {
        mapper.markOutboxFailed(eventId, reason);
    }
}
