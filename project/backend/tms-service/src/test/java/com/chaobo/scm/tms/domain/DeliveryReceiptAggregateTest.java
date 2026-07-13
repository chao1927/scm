package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliveryReceiptAggregateTest {
    @Test
    void signedReceiptPublishesSignedEvent() {
        DeliveryReceiptAggregate receipt = DeliveryReceiptAggregate.record("RCP1", "WB1",
                DeliveryReceiptAggregate.SIGNED, "李四", LocalDateTime.parse("2026-07-12T12:00:00"),
                null, "oss://proof/RCP1.jpg");

        assertThat(receipt.pullEvents()).extracting(TmsEvent::eventType)
                .containsExactly("TransportSigned");
    }

    @Test
    void rejectedReceiptRequiresReason() {
        assertThatThrownBy(() -> DeliveryReceiptAggregate.record("RCP1", "WB1",
                DeliveryReceiptAggregate.REJECTED, null, LocalDateTime.parse("2026-07-12T12:00:00"),
                null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reject reason");
    }
}
