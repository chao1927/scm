package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryEventEnvelope;
import com.chaobo.scm.inventory.application.InventoryInboundEventStore;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/**
 * MyBatis 入站事件可靠性存储。
 *
 * <p>数据库唯一键解决并发重复投递，游标更新行数解决同一聚合并发推进；应用层据此决定幂等返回或重试。
 *
 * @author SCM Team
 */
@Repository
public class MyBatisInventoryInboundEventStore implements InventoryInboundEventStore {

    private final InventoryReliableEventMapper mapper;

    public MyBatisInventoryInboundEventStore(InventoryReliableEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public InboxEvent find(
            String sourceSystem,
            String eventId,
            String consumerName) {
        return toInbox(mapper.findInbox(sourceSystem, eventId, consumerName));
    }

    @Override
    public InboxEvent register(
            InventoryEventEnvelope event,
            String consumerName,
            String envelopeJson) {
        try {
            mapper.insertInbox(new InventoryReliableEventMapper.InboxInsert(
                    event.sourceSystem(),
                    event.eventId(),
                    event.eventType(),
                    event.eventVersion(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.aggregateVersion(),
                    consumerName,
                    envelopeJson));
        } catch (DuplicateKeyException ignored) {
            // Broker 并发投递同一 eventId 时读取唯一键对应记录，不能把重复事件当成消费失败。
        }
        return find(event.sourceSystem(), event.eventId(), consumerName);
    }

    @Override
    public void markSucceeded(long inboxId) {
        mapper.markSucceeded(inboxId);
    }

    @Override
    public void markIgnored(long inboxId, String reason) {
        mapper.markIgnored(inboxId, reason);
    }

    @Override
    public void markFailed(long inboxId, String reason) {
        mapper.markFailed(inboxId, reason);
    }

    @Override
    public void markWaitingReplay(long inboxId, String reason) {
        mapper.markWaitingReplay(inboxId, reason);
    }

    @Override
    public EventCursor findCursor(
            String sourceSystem,
            String aggregateType,
            String aggregateId,
            String consumerName) {
        InventoryReliableEventMapper.CursorRow row = mapper.findCursor(
                sourceSystem,
                aggregateType,
                aggregateId,
                consumerName);
        return row == null ? null : new EventCursor(row.aggregateVersion(), row.eventCode());
    }

    @Override
    public void createCursor(
            InventoryEventEnvelope event,
            String consumerName) {
        mapper.insertCursor(cursor(event, consumerName));
    }

    @Override
    public boolean advanceCursor(
            InventoryEventEnvelope event,
            String consumerName,
            long expectedVersion) {
        return mapper.updateCursor(cursor(event, consumerName), expectedVersion) == 1;
    }

    private static InventoryReliableEventMapper.CursorInsert cursor(
            InventoryEventEnvelope event,
            String consumerName) {
        return new InventoryReliableEventMapper.CursorInsert(
                event.sourceSystem(),
                event.aggregateType(),
                event.aggregateId(),
                consumerName,
                event.aggregateVersion(),
                event.eventId());
    }

    private static InboxEvent toInbox(InventoryReliableEventMapper.InboxRow row) {
        return row == null ? null : new InboxEvent(
                row.id(),
                row.sourceSystem(),
                row.eventCode(),
                row.eventType(),
                row.eventVersion(),
                row.aggregateType(),
                row.aggregateId(),
                row.aggregateVersion(),
                row.consumerName(),
                row.envelopeJson(),
                row.status(),
                row.retryCount(),
                row.lastError(),
                row.ignoredReason());
    }
}
