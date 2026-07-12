package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AfterSaleAggregateTest {
    @Test
    void requestsAndCompletesRefund() {
        AfterSaleAggregate aggregate = AfterSaleAggregate.create(
                "AS-1", "SO-1", "FUL-1", new BigDecimal("20.00"), "仅退款");

        aggregate.approve("符合规则");
        aggregate.requestRefund();
        aggregate.markRefunded(new BigDecimal("20.00"));
        aggregate.complete();

        assertThat(aggregate.status()).isEqualTo(AfterSaleAggregate.COMPLETED);
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType)
                .containsExactly("AfterSaleCreated", "AfterSaleApproved", "RefundRequested",
                        "RefundCompleted", "AfterSaleCompleted");
    }

    @Test
    void refundCannotExceedRequestedAmount() {
        AfterSaleAggregate aggregate = AfterSaleAggregate.create(
                "AS-1", "SO-1", "FUL-1", new BigDecimal("20.00"), "仅退款");
        aggregate.approve("符合规则");
        aggregate.requestRefund();

        assertThatThrownBy(() -> aggregate.markRefunded(new BigDecimal("20.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refund amount");
    }
}
