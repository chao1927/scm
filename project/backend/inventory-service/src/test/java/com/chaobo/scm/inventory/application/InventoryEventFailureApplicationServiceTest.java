package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 事件失败治理应用服务测试。
 *
 * @author SCM Team
 */
class InventoryEventFailureApplicationServiceTest {

    @Test
    void inboundReplayUsesStoredEnvelopeAndSameIdempotencyKeyOnlyRunsOnce() {
        InventoryEventEnvelopeCodec codec =
                new InventoryEventEnvelopeCodec(new ObjectMapper());
        InventoryEventEnvelope envelope = TestEvents.event("E-FAILED", 1L);
        MemoryFailureStore store = new MemoryFailureStore(
                new InventoryEventFailureStore.FailureEvent(
                        InventoryEventFailureStore.Direction.INBOUND,
                        "E-FAILED",
                        "WmsPutawayCompleted",
                        "1.0",
                        "InboundOrder",
                        "IN-1",
                        3,
                        2,
                        "temporary failure",
                        codec.encode(envelope)));
        int[] consumed = {0};
        InventoryInboundEventApplicationService inbound =
                new InventoryInboundEventApplicationService(
                        new ReplayInboxStore(),
                        event -> consumed[0]++,
                        new ImmediateInventoryEventTransactions());
        InventoryEventFailureApplicationService service = service(store, inbound, codec);

        InventoryEventFailureApplicationService.ReplayResult first = service.replay(
                new InventoryEventFailureApplicationService.ReplayCommand(
                        InventoryEventFailureStore.Direction.INBOUND,
                        "E-FAILED",
                        "manual-replay-1",
                        "fix downstream",
                        1001L));
        InventoryEventFailureApplicationService.ReplayResult duplicate = service.replay(
                new InventoryEventFailureApplicationService.ReplayCommand(
                        InventoryEventFailureStore.Direction.INBOUND,
                        "E-FAILED",
                        "manual-replay-1",
                        "fix downstream",
                        1001L));

        assertThat(first.replayed()).isTrue();
        assertThat(duplicate.replayed()).isFalse();
        assertThat(consumed[0]).isEqualTo(1);
        assertThat(store.succeeded).isTrue();
    }

    @Test
    void replayRejectsNonFailedEvent() {
        InventoryEventEnvelopeCodec codec =
                new InventoryEventEnvelopeCodec(new ObjectMapper());
        MemoryFailureStore store = new MemoryFailureStore(null);
        InventoryInboundEventApplicationService inbound =
                new InventoryInboundEventApplicationService(
                        new ReplayInboxStore(),
                        event -> { },
                        new ImmediateInventoryEventTransactions());
        InventoryEventFailureApplicationService service = service(store, inbound, codec);

        assertThatThrownBy(() -> service.replay(
                new InventoryEventFailureApplicationService.ReplayCommand(
                        InventoryEventFailureStore.Direction.INBOUND,
                        "E-MISSING",
                        "manual-replay-2",
                        "try missing event",
                        1001L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("失败事件不存在");
    }

    private static InventoryEventFailureApplicationService service(
            InventoryEventFailureStore store,
            InventoryInboundEventApplicationService inbound,
            InventoryEventEnvelopeCodec codec) {
        return new InventoryEventFailureApplicationService(
                store,
                inbound,
                codec,
                new ImmediateInventoryEventTransactions(),
                new StaticListableBeanFactory().getBeanProvider(
                        InventoryOutboundEventReplayer.class));
    }

    private static final class MemoryFailureStore implements InventoryEventFailureStore {

        private final FailureEvent event;
        private boolean replayRegistered;
        private boolean succeeded;

        private MemoryFailureStore(FailureEvent event) {
            this.event = event;
        }

        @Override
        public FailurePage failures(Direction direction, int offset, int limit) {
            return new FailurePage(event == null ? 0L : 1L, event == null
                    ? List.of()
                    : List.of(event));
        }

        @Override
        public FailureEvent findFailure(Direction direction, String eventCode) {
            return event;
        }

        @Override
        public ReplayRegistration registerReplay(
                String idempotencyKey,
                Direction direction,
                String eventCode,
                String reason,
                long operatorId) {
            if (replayRegistered) {
                return new ReplayRegistration(1L, false, 2);
            }
            replayRegistered = true;
            return new ReplayRegistration(1L, true, 1);
        }

        @Override
        public void markReplaySucceeded(long replayId) {
            succeeded = true;
        }

        @Override
        public void markReplayFailed(long replayId, String reason) {
            // 失败路径由专门异常测试覆盖，这里不需要额外状态。
        }
    }

    private static final class ReplayInboxStore implements InventoryInboundEventStore {

        private InboxEvent inbox;
        private EventCursor cursor;

        @Override
        public InboxEvent find(String sourceSystem, String eventId, String consumerName) {
            return inbox;
        }

        @Override
        public InboxEvent register(
                InventoryEventEnvelope event,
                String consumerName,
                String envelopeJson) {
            inbox = new InboxEvent(
                    1L, event.sourceSystem(), event.eventId(), event.eventType(),
                    event.eventVersion(), event.aggregateType(), event.aggregateId(),
                    event.aggregateVersion(), consumerName, envelopeJson,
                    STATUS_FAILED, 1, "old failure", null);
            return inbox;
        }

        @Override
        public void markSucceeded(long inboxId) {
            inbox = copy(STATUS_SUCCEEDED);
        }

        @Override
        public void markIgnored(long inboxId, String reason) {
            inbox = copy(STATUS_IGNORED);
        }

        @Override
        public void markFailed(long inboxId, String reason) {
            inbox = copy(STATUS_FAILED);
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

        private InboxEvent copy(int status) {
            return new InboxEvent(
                    inbox.id(), inbox.sourceSystem(), inbox.eventId(), inbox.eventType(),
                    inbox.eventVersion(), inbox.aggregateType(), inbox.aggregateId(),
                    inbox.aggregateVersion(), inbox.consumerName(), inbox.envelopeJson(),
                    status, inbox.retryCount(), inbox.lastError(), inbox.ignoredReason());
        }
    }

    private static final class TestEvents {

        private static InventoryEventEnvelope event(String eventId, long aggregateVersion) {
            return new InventoryEventEnvelope(
                    eventId, "WmsPutawayCompleted", "1.0", "WMS", "WMS",
                    "InboundOrder", "IN-1", aggregateVersion, "IN-1", eventId,
                    "2026-07-30T10:00:00+08:00", "TRACE-1",
                    java.util.Map.of("inboundOrderNo", "IN-1"));
        }
    }
}
