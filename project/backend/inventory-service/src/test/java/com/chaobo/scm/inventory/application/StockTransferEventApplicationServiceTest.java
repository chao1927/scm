package com.chaobo.scm.inventory.application;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 调拨兼容入口统一使用版本化 Inbox 的测试。
 *
 * @author SCM Team
 */
class StockTransferEventApplicationServiceTest {

    @Test
    void duplicateTransferFactOnlyInvokesUnifiedProcessorOnce() {
        MemoryStore store = new MemoryStore();
        int[] calls = {0};
        InventoryInboundEventApplicationService inbound =
                new InventoryInboundEventApplicationService(
                        store,
                        event -> calls[0]++,
                        new ImmediateTransactions());
        StockTransferEventApplicationService service =
                new StockTransferEventApplicationService(
                        inbound,
                        new InventoryEventEnvelopeCodec(new ObjectMapper()));
        StockTransferEventApplicationService.EventEnvelope event =
                new StockTransferEventApplicationService.EventEnvelope(
                        "WMS",
                        "E-1",
                        "TransferOutboundCompleted",
                        "TRF-1",
                        new BigDecimal("5"),
                        false,
                        1);

        assertThat(service.consume(event).duplicated()).isFalse();
        assertThat(service.consume(event).duplicated()).isTrue();
        assertThat(calls[0]).isEqualTo(1);
    }

    private static final class ImmediateTransactions implements InventoryEventTransactions {

        @Override
        public <T> T required(Supplier<T> action) {
            return action.get();
        }

        @Override
        public <T> T requiresNew(Supplier<T> action) {
            return action.get();
        }
    }

    private static final class MemoryStore implements InventoryInboundEventStore {

        private final Map<String, InboxEvent> inboxes = new HashMap<>();
        private EventCursor cursor;

        @Override
        public InboxEvent find(String sourceSystem, String eventId, String consumerName) {
            return inboxes.get(eventId);
        }

        @Override
        public InboxEvent register(
                InventoryEventEnvelope event,
                String consumerName,
                String envelopeJson) {
            InboxEvent row = new InboxEvent(
                    1L,
                    event.sourceSystem(),
                    event.eventId(),
                    event.eventType(),
                    event.eventVersion(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.aggregateVersion(),
                    consumerName,
                    envelopeJson,
                    STATUS_PROCESSING,
                    0,
                    null,
                    null);
            inboxes.putIfAbsent(event.eventId(), row);
            return inboxes.get(event.eventId());
        }

        @Override
        public void markSucceeded(long inboxId) {
            InboxEvent row = inboxes.get("E-1");
            inboxes.put("E-1", copy(row, STATUS_SUCCEEDED));
        }

        @Override
        public void markIgnored(long inboxId, String reason) {
            InboxEvent row = inboxes.get("E-1");
            inboxes.put("E-1", copy(row, STATUS_IGNORED));
        }

        @Override
        public void markFailed(long inboxId, String reason) {
            InboxEvent row = inboxes.get("E-1");
            inboxes.put("E-1", copy(row, STATUS_FAILED));
        }

        @Override
        public void markWaitingReplay(long inboxId, String reason) {
            InboxEvent row = inboxes.get("E-1");
            inboxes.put("E-1", copy(row, STATUS_WAITING_REPLAY));
        }

        @Override
        public EventCursor findCursor(
                String sourceSystem,
                String aggregateType,
                String aggregateId,
                String consumerName) {
            return cursor;
        }

        @Override
        public void createCursor(InventoryEventEnvelope event, String consumerName) {
            cursor = new EventCursor(event.aggregateVersion(), event.eventId());
        }

        @Override
        public boolean advanceCursor(
                InventoryEventEnvelope event,
                String consumerName,
                long expectedVersion) {
            cursor = new EventCursor(event.aggregateVersion(), event.eventId());
            return true;
        }

        private static InboxEvent copy(InboxEvent row, int status) {
            return new InboxEvent(
                    row.id(),
                    row.sourceSystem(),
                    row.eventId(),
                    row.eventType(),
                    row.eventVersion(),
                    row.aggregateType(),
                    row.aggregateId(),
                    row.aggregateVersion(),
                    row.consumerName(),
                    row.envelopeJson(),
                    status,
                    row.retryCount(),
                    row.lastError(),
                    row.ignoredReason());
        }
    }
}
