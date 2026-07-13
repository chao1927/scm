package com.chaobo.scm.integration.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegrationAggregateTest {
    @Test
    void routeCanBeDisabledWithVersion() {
        IntegrationRouteAggregate route = IntegrationRouteAggregate.create(
                "IR100001", "PurchaseOrderReleased", "PURCHASE", "WMS", "MQ");

        route.disable(1);

        assertThat(route.status()).isEqualTo(IntegrationRouteAggregate.DISABLED);
        assertThat(route.version()).isEqualTo(2);
    }

    @Test
    void messageFailsIntoDeadLetterAfterMaxRetry() {
        IntegrationMessageAggregate message = IntegrationMessageAggregate.create(
                "IM200001", "PurchaseOrderReleased", "PURCHASE", "WMS", "PO-1", "idem-1", "{}");

        assertThat(message.markFailed("timeout", 3, 1)).isFalse();
        assertThat(message.status()).isEqualTo(IntegrationMessageAggregate.FAILED);
        message.retry(2);
        assertThat(message.markFailed("timeout", 3, 3)).isFalse();
        message.retry(4);
        assertThat(message.markFailed("timeout", 3, 5)).isTrue();

        assertThat(message.status()).isEqualTo(IntegrationMessageAggregate.DEAD_LETTER);
        assertThat(message.retryCount()).isEqualTo(3);
    }

    @Test
    void dispatchedMessageCannotRetry() {
        IntegrationMessageAggregate message = IntegrationMessageAggregate.create(
                "IM200001", "PurchaseOrderReleased", "PURCHASE", "WMS", "PO-1", "idem-1", "{}");
        message.markDispatched(1);

        assertThatThrownBy(() -> message.retry(2)).isInstanceOf(IllegalStateException.class);
    }
}
