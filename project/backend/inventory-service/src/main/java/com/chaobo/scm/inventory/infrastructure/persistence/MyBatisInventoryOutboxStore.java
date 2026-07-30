package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryOutboxStore;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * MyBatis 库存 Outbox 投递存储。
 *
 * @author SCM Team
 */
@Repository
public class MyBatisInventoryOutboxStore implements InventoryOutboxStore {

    private final InventoryOutboxMapper mapper;

    public MyBatisInventoryOutboxStore(InventoryOutboxMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<OutboxEvent> pending(int limit, int maxRetries) {
        return mapper.pending(limit, maxRetries).stream()
                .map(MyBatisInventoryOutboxStore::toEvent)
                .toList();
    }

    @Override
    public OutboxEvent findFailed(String eventCode) {
        InventoryOutboxMapper.OutboxRow row = mapper.findFailed(eventCode);
        return row == null ? null : toEvent(row);
    }

    @Override
    public void markPublished(long eventId) {
        mapper.markPublished(eventId);
    }

    @Override
    public void markFailed(long eventId, String reason) {
        mapper.markFailed(eventId, reason);
    }

    private static OutboxEvent toEvent(InventoryOutboxMapper.OutboxRow row) {
        return new OutboxEvent(
                row.id(),
                row.eventCode(),
                row.eventType(),
                row.eventVersion(),
                row.aggregateType(),
                row.aggregateId(),
                row.payloadJson(),
                row.status(),
                row.retryCount());
    }
}
