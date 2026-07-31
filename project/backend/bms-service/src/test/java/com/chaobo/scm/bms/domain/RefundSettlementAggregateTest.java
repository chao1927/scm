package com.chaobo.scm.bms.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundSettlementAggregateTest {

    @Test
    void unknownPaymentKeepsRefundOccupiedUntilLateSuccess() {
        var refund = BmsDomain.RefundSettlementAggregate.request(
            "RF-1", "B-1", new BigDecimal("60.00"), new BigDecimal("100.00"));

        refund.markConfirmationPending("payment timeout", 1);

        assertThat(refund.status())
            .isEqualTo(BmsDomain.RefundSettlementAggregate.CONFIRMATION_PENDING);
        refund.finish(2);
        assertThat(refund.status())
            .isEqualTo(BmsDomain.RefundSettlementAggregate.FINISHED);
    }

    @Test
    void manualCloseRequiresPendingStateAndReason() {
        var refund = BmsDomain.RefundSettlementAggregate.request(
            "RF-2", "B-1", new BigDecimal("20.00"), new BigDecimal("100.00"));

        assertThatThrownBy(() -> refund.closeManually("verified unpaid", 1))
            .isInstanceOf(IllegalStateException.class);

        refund.markConfirmationPending("payment timeout", 1);
        assertThatThrownBy(() -> refund.closeManually(" ", 2))
            .isInstanceOf(IllegalArgumentException.class);
        refund.closeManually("verified unpaid", 2);
        assertThat(refund.status())
            .isEqualTo(BmsDomain.RefundSettlementAggregate.CLOSED);
    }

    @Test
    void terminalRefundRejectsConflictingLateReceipt() {
        var refund = BmsDomain.RefundSettlementAggregate.request(
            "RF-3", "B-1", new BigDecimal("20.00"), new BigDecimal("100.00"));
        refund.finish(1);

        assertThatThrownBy(() -> refund.fail("late failure", 2))
            .isInstanceOf(IllegalStateException.class);
    }
}
