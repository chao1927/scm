package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FulfillmentAggregateTest {
    @Test
    void allocatesWarehouseAndRequestsReservation() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create(
                "FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD",
                List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))));

        aggregate.requestReservation("RES-REF-1");
        aggregate.recordReservationSuccess("INV-RES-1", new BigDecimal("2"));
        aggregate.markOutboundIssued("OUT-1");

        assertThat(aggregate.status()).isEqualTo(FulfillmentAggregate.OUTBOUND_ISSUED);
        assertThat(aggregate.reservationNo()).isEqualTo("INV-RES-1");
        assertThat(aggregate.outboundNo()).isEqualTo("OUT-1");
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType)
                .contains("FulfillmentOrderCreated", "StockReservationRequested",
                        "FulfillmentInventoryReserved", "OutboundInstructionIssued");
    }

    @Test
    void rejectsWarehouseChangeAfterReservation() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create(
                "FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD",
                List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))));
        aggregate.requestReservation("RES-REF-1");

        assertThatThrownBy(() -> aggregate.changeWarehouse(101L, "WH-2", "库存不足换仓"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pending reservation");
    }

    @Test
    void splitKeepsQuantityConserved() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create(
                "FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD",
                List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("5"))));

        FulfillmentAggregate child = aggregate.split("FUL-2",
                List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))), "拆分到备用仓");

        assertThat(aggregate.lines().getFirst().quantity()).isEqualByComparingTo("3");
        assertThat(child.lines().getFirst().quantity()).isEqualByComparingTo("2");
        assertThat(child.status()).isEqualTo(FulfillmentAggregate.PENDING_RESERVATION);
    }

    @Test
    void cannotIssueOutboundBeforeReservation() {
        FulfillmentAggregate aggregate = FulfillmentAggregate.create(
                "FUL-1", "SO-1", "TMALL", 88L, 100L, "WH-1", "STANDARD",
                List.of(new FulfillmentAggregate.Line("SKU-1", new BigDecimal("2"))));

        assertThatThrownBy(() -> aggregate.markOutboundIssued("OUT-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reservation");
    }
}
