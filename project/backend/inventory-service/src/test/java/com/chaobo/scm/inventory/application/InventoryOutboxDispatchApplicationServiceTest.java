package com.chaobo.scm.inventory.application;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 库存 Outbox 到消息代理的可靠投递测试。
 *
 * <p>成功发送后才能标记已发布；代理异常必须保留失败状态，并且实际消息体必须是版本化标准信封。
 *
 * @author SCM Team
 */
class InventoryOutboxDispatchApplicationServiceTest {

    @Test
    void publishesVersionedEnvelopeBeforeMarkingOutboxSucceeded() {
        MemoryOutbox store = new MemoryOutbox();
        MemoryBroker broker = new MemoryBroker();
        InventoryEventEnvelopeCodec codec =
                new InventoryEventEnvelopeCodec(new ObjectMapper());
        InventoryOutboxDispatchApplicationService service =
                new InventoryOutboxDispatchApplicationService(store, broker, codec);

        InventoryOutboxDispatchApplicationService.DispatchResult result =
                service.dispatch(10, 8);

        assertThat(result.published()).isEqualTo(1);
        assertThat(store.published).containsExactly(1L);
        assertThat(store.failed).isEmpty();
        InventoryEventEnvelope envelope = codec.decode(broker.messages.get(0).envelopeJson());
        assertThat(envelope.eventVersion()).isEqualTo("1.0");
        assertThat(envelope.eventId()).isEqualTo("INV-E-1");
        assertThat(envelope.aggregateVersion()).isEqualTo(3L);
        assertThat(envelope.payload()).containsEntry("availableQty", "7");
    }

    @Test
    void brokerFailureLeavesOutboxRetryable() {
        MemoryOutbox store = new MemoryOutbox();
        InventoryMessageBroker broker = message -> {
            throw new IllegalStateException("RocketMQ unavailable");
        };
        InventoryOutboxDispatchApplicationService service =
                new InventoryOutboxDispatchApplicationService(
                        store,
                        broker,
                        new InventoryEventEnvelopeCodec(new ObjectMapper()));

        InventoryOutboxDispatchApplicationService.DispatchResult result =
                service.dispatch(10, 8);

        assertThat(result.failed()).isEqualTo(1);
        assertThat(store.published).isEmpty();
        assertThat(store.failed).containsExactly("1:RocketMQ unavailable");
    }

    @Test
    void manualReplayPublishesOnlySpecifiedFailedOutboxEvent() {
        MemoryOutbox store = new MemoryOutbox();
        MemoryBroker broker = new MemoryBroker();
        InventoryOutboxDispatchApplicationService service =
                new InventoryOutboxDispatchApplicationService(
                        store,
                        broker,
                        new InventoryEventEnvelopeCodec(new ObjectMapper()));

        service.replay("INV-E-1");

        assertThat(broker.messages).hasSize(1);
        assertThat(store.published).containsExactly(1L);
        assertThat(store.failed).isEmpty();
    }

    private static final class MemoryBroker implements InventoryMessageBroker {

        private final List<InventoryOutboxMessage> messages = new ArrayList<>();

        @Override
        public void publish(InventoryOutboxMessage message) {
            messages.add(message);
        }
    }

    private static final class MemoryOutbox implements InventoryOutboxStore {

        private final List<Long> published = new ArrayList<>();
        private final List<String> failed = new ArrayList<>();

        @Override
        public List<OutboxEvent> pending(int limit, int maxRetries) {
            return List.of(new OutboxEvent(
                    1L,
                    "INV-E-1",
                    "InventoryChanged",
                    "1.0",
                    "InventoryAccount",
                    "STOCK-1",
                    "{\"availableQty\":\"7\",\"version\":3}",
                    1,
                    0));
        }

        @Override
        public OutboxEvent findFailed(String eventCode) {
            OutboxEvent event = pending(1, 1).get(0);
            return new OutboxEvent(
                    event.id(), event.eventCode(), event.eventType(), event.eventVersion(),
                    event.aggregateType(), event.aggregateId(), event.payloadJson(), 3,
                    event.retryCount());
        }

        @Override
        public void markPublished(long eventId) {
            published.add(eventId);
        }

        @Override
        public void markFailed(long eventId, String reason) {
            failed.add(eventId + ":" + reason);
        }
    }
}
