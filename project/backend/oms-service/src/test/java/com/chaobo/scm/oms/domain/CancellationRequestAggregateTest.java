package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancellationRequestAggregateTest {
    @Test
    void cancellationWaitsForWmsAndStockRelease() {
        CancellationRequestAggregate aggregate = CancellationRequestAggregate.create(
                "CAN-1", "SO-1", "FUL-1", "OUT-1", "RES-1", "客户不需要");

        aggregate.approve("客服同意");
        aggregate.process(true);
        aggregate.markWmsCancelled();
        aggregate.markStockReleased();

        assertThat(aggregate.status()).isEqualTo(CancellationRequestAggregate.COMPLETED);
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType)
                .containsExactly("CancelRequestCreated", "CancelRequestApproved", "WmsCancelRequested",
                        "StockReleaseRequested", "SalesOrderCanceled");
    }

    @Test
    void shippedOrderCannotBeCompletedByCancellation() {
        CancellationRequestAggregate aggregate = CancellationRequestAggregate.create(
                "CAN-1", "SO-1", "FUL-1", "OUT-1", "RES-1", "客户不需要");
        aggregate.approve("客服同意");
        aggregate.process(true);

        assertThatThrownBy(() -> aggregate.markStockReleased())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WMS");
    }
}
