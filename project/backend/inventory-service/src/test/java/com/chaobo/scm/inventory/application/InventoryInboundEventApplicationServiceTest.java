package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 入站事件版本、幂等、顺序和失败重放测试。
 *
 * <p>测试只替换事务和持久化端口，业务处理器使用有状态假实现，以最终记账次数证明重复、乱序和重放
 * 不会造成重复库存变化。
 *
 * @author SCM Team
 */
class InventoryInboundEventApplicationServiceTest {

    @Test
    void unknownEnvelopeVersionFailsClosedAndPersistsFailure() {
        MemoryStore store = new MemoryStore();
        CountingProcessor processor = new CountingProcessor();
        InventoryInboundEventApplicationService service = service(store, processor);
        InventoryEventEnvelope event = event("E-1", "2.0", 1L);

        assertThatThrownBy(() -> service.consume(event, "{}"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的事件版本");

        assertThat(store.inboxes.get("E-1").status())
                .isEqualTo(InventoryInboundEventStore.STATUS_FAILED);
        assertThat(processor.calls).isZero();
    }

    @Test
    void duplicateAndOutOfOrderReplayNeverApplyInventoryTwice() {
        MemoryStore store = new MemoryStore();
        CountingProcessor processor = new CountingProcessor();
        InventoryInboundEventApplicationService service = service(store, processor);
        InventoryEventEnvelope second = event("E-2", "1.0", 2L);

        assertThatThrownBy(() -> service.consume(second, "{}"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("事件乱序");
        assertThat(processor.calls).isZero();

        InventoryEventEnvelope first = event("E-1", "1.0", 1L);
        assertThat(service.consume(first, "{}").duplicated()).isFalse();
        assertThat(processor.calls).isEqualTo(1);

        assertThat(service.consume(second, "{}").duplicated()).isFalse();
        assertThat(processor.calls).isEqualTo(2);

        assertThat(service.consume(second, "{}").duplicated()).isTrue();
        assertThat(processor.calls).isEqualTo(2);
    }

    private static InventoryInboundEventApplicationService service(
            MemoryStore store,
            CountingProcessor processor) {
        return new InventoryInboundEventApplicationService(
                store,
                processor,
                new ImmediateTransactions());
    }

    private static InventoryEventEnvelope event(
            String eventId,
            String eventVersion,
            long aggregateVersion) {
        return new InventoryEventEnvelope(
                eventId,
                "InboundOrderPutawayCompleted",
                eventVersion,
                "WMS",
                "WMS",
                "InboundOrder",
                "IN-1",
                aggregateVersion,
                "IN-1",
                "WMS:IN-1:" + aggregateVersion,
                "2026-07-30T10:00:00+08:00",
                "TRACE-1",
                Map.of(
                        "ownerId", 88,
                        "warehouseId", 99,
                        "sku", "SKU-1",
                        "putawayQty", 1,
                        "sourceNo", "IN-1"));
    }

    private static final class CountingProcessor implements InventoryInboundEventProcessor {

        private int calls;

        @Override
        public void process(InventoryEventEnvelope event) {
            calls++;
        }
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
        private final Map<String, EventCursor> cursors = new HashMap<>();
        private long ids;

        @Override
        public InboxEvent find(String sourceSystem, String eventId, String consumerName) {
            return inboxes.get(eventId);
        }

        @Override
        public InboxEvent register(
                InventoryEventEnvelope event,
                String consumerName,
                String envelopeJson) {
            return inboxes.computeIfAbsent(event.eventId(), ignored -> new InboxEvent(
                    ++ids,
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
                    null));
        }

        @Override
        public void markSucceeded(long inboxId) {
            replace(inboxId, STATUS_SUCCEEDED, null, null);
        }

        @Override
        public void markIgnored(long inboxId, String reason) {
            replace(inboxId, STATUS_IGNORED, null, reason);
        }

        @Override
        public void markFailed(long inboxId, String reason) {
            replace(inboxId, STATUS_FAILED, reason, null);
        }

        @Override
        public EventCursor findCursor(
                String sourceSystem,
                String aggregateType,
                String aggregateId,
                String consumerName) {
            return cursors.get(cursorKey(sourceSystem, aggregateType, aggregateId, consumerName));
        }

        @Override
        public void createCursor(
                InventoryEventEnvelope event,
                String consumerName) {
            cursors.put(
                    cursorKey(
                            event.sourceSystem(),
                            event.aggregateType(),
                            event.aggregateId(),
                            consumerName),
                    new EventCursor(event.aggregateVersion(), event.eventId()));
        }

        @Override
        public boolean advanceCursor(
                InventoryEventEnvelope event,
                String consumerName,
                long expectedVersion) {
            String key = cursorKey(
                    event.sourceSystem(),
                    event.aggregateType(),
                    event.aggregateId(),
                    consumerName);
            EventCursor current = cursors.get(key);
            if (current == null || current.aggregateVersion() != expectedVersion) {
                return false;
            }
            cursors.put(key, new EventCursor(event.aggregateVersion(), event.eventId()));
            return true;
        }

        private void replace(
                long inboxId,
                int status,
                String error,
                String ignoredReason) {
            inboxes.replaceAll((eventId, row) -> row.id() == inboxId
                    ? new InboxEvent(
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
                            row.retryCount() + (status == STATUS_FAILED ? 1 : 0),
                            error,
                            ignoredReason)
                    : row);
        }

        private static String cursorKey(
                String sourceSystem,
                String aggregateType,
                String aggregateId,
                String consumerName) {
            return sourceSystem + ":" + aggregateType + ":" + aggregateId + ":" + consumerName;
        }
    }
}
