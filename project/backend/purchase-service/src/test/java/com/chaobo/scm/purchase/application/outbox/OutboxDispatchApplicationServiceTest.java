package com.chaobo.scm.purchase.application.outbox;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxDispatchApplicationServiceTest {
    @Test
    void dispatchMarksPublishedAfterBrokerSuccess() {
        var store = new Store();
        var service = new OutboxDispatchApplicationService(store, message -> {
        });

        service.dispatch(message());

        assertThat(store.published).isEqualTo(1);
        assertThat(store.failed).isZero();
    }

    @Test
    void dispatchMarksFailedAfterBrokerFailure() {
        var store = new Store();
        var service = new OutboxDispatchApplicationService(store, message -> {
            throw new IllegalStateException("MQ不可用");
        });

        service.dispatch(message());

        assertThat(store.failed).isEqualTo(1);
        assertThat(store.reason).isEqualTo("MQ不可用");
    }

    private OutboxMessage message() {
        return new OutboxMessage(1, "PUR-1", "PurchaseOrderPublished", "PURCHASE_ORDER",
                "1001", "{}", 0);
    }

    private static final class Store implements OutboxDispatchPort {
        private int published;
        private int failed;
        private String reason;

        @Override
        public List<OutboxMessage> claim(int batchSize, int maxRetries) {
            return List.of();
        }

        @Override
        public void markPublished(long eventId) {
            published++;
        }

        @Override
        public void markFailed(long eventId, String reason) {
            failed++;
            this.reason = reason;
        }
    }
}
