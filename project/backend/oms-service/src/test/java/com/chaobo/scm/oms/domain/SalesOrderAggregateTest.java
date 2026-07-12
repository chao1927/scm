package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalesOrderAggregateTest {
    @Test
    void createSalesOrderCalculatesAmountAndProducesEvents() {
        SalesOrderAggregate aggregate = SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市",
                List.of(new SalesOrderAggregate.OrderLine("SKU1", 2, new BigDecimal("12.50"))));

        assertThat(aggregate.totalAmount()).isEqualByComparingTo("25.00");
        assertThat(aggregate.status()).isEqualTo(SalesOrderAggregate.PENDING_REVIEW);
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType)
                .containsExactly("ChannelOrderReceived", "SalesOrderCreated");
    }

    @Test
    void rejectInvalidLines() {
        assertThatThrownBy(() -> SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市",
                List.of(new SalesOrderAggregate.OrderLine("SKU1", 0, BigDecimal.ONE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid order line");
    }

    @Test
    void reviewStateMachineRejectsDuplicateReview() {
        SalesOrderAggregate aggregate = SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市",
                List.of(new SalesOrderAggregate.OrderLine("SKU1", 1, BigDecimal.ONE)));

        aggregate.approve("ok");

        assertThat(aggregate.status()).isEqualTo(SalesOrderAggregate.APPROVED);
        assertThatThrownBy(() -> aggregate.intercept("risk"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not pending review");
    }

    @Test
    void interceptRequiresReason() {
        SalesOrderAggregate aggregate = SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市",
                List.of(new SalesOrderAggregate.OrderLine("SKU1", 1, BigDecimal.ONE)));

        assertThatThrownBy(() -> aggregate.intercept(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");
    }
}
