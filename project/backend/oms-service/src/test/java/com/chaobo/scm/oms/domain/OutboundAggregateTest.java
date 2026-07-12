package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundAggregateTest {
    @Test
    void dispatchesAndTracksWmsAcceptance() {
        OutboundAggregate aggregate = OutboundAggregate.create("OUT-1", "FUL-1", "SO-1", 100L, "WH-1");

        aggregate.dispatch();
        aggregate.markWmsAccepted("WMS-1");

        assertThat(aggregate.status()).isEqualTo(OutboundAggregate.WMS_ACCEPTED);
        assertThat(aggregate.wmsOrderNo()).isEqualTo("WMS-1");
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType)
                .containsExactly("OutboundOrderCreated", "OutboundInstructionIssued", "WmsOutboundAccepted");
    }

    @Test
    void shippedOutboundCannotBeCancelled() {
        OutboundAggregate aggregate = OutboundAggregate.create("OUT-1", "FUL-1", "SO-1", 100L, "WH-1");
        aggregate.dispatch();
        aggregate.markWmsAccepted("WMS-1");
        aggregate.markShipped();

        assertThatThrownBy(() -> aggregate.requestCancel("客户取消"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shipped");
    }
}
