package com.chaobo.scm.bms.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BmsDomainTest {
    @Test
    void publishedRuleCalculatesTaxIncludedAmount() {
        BmsDomain.BillingRuleAggregate rule = BmsDomain.BillingRuleAggregate.create("BR1", "BO1", "FREIGHT",
                new BigDecimal("10.0000"), new BigDecimal("0.1000"), LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-12-31"));
        rule.publish(1);

        BmsDomain.ChargeAmount amount = rule.calculate(new BigDecimal("2.5"));

        assertThat(amount.amount()).isEqualByComparingTo("25.00");
        assertThat(amount.taxAmount()).isEqualByComparingTo("2.50");
        assertThat(amount.totalAmount()).isEqualByComparingTo("27.50");
        assertThat(rule.effectiveOn(LocalDate.parse("2026-07-01"))).isTrue();
    }

    @Test
    void confirmedChargeCannotRecalculate() {
        BmsDomain.ChargeDetailAggregate charge = BmsDomain.ChargeDetailAggregate.create("CD1", "CS1", "BO1",
                "FREIGHT", "BR1", BigDecimal.ONE, BigDecimal.TEN,
                new BmsDomain.ChargeAmount(BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("11.00")));
        charge.confirm();

        assertThatThrownBy(() -> charge.recalculate(BigDecimal.TEN, BigDecimal.ONE,
                new BmsDomain.ChargeAmount(BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN), charge.version()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("adjustment");
    }

    @Test
    void reconciliationAndInvoiceProtectAmountInvariants() {
        BmsDomain.ReconciliationAggregate reconciliation = BmsDomain.ReconciliationAggregate.create("RC1", "BO1",
                "2026-07", new BigDecimal("100.00"));

        assertThatThrownBy(() -> reconciliation.confirm(new BigDecimal("99.99"), reconciliation.version()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BmsDomain.InvoiceAggregate.request("IV1", "BL1", new BigDecimal("120.00"),
                new BigDecimal("100.00"))).isInstanceOf(IllegalArgumentException.class);
    }
}
