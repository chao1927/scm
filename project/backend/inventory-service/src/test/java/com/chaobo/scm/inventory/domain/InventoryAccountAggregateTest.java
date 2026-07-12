package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryAccountAggregateTest {
    @Test
    void reserveReleaseFreezeAndOutboundProtectQuantityInvariant() {
        var account = new InventoryAccountAggregate(1, 1, 1, "SKU", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

        account.receive(BigDecimal.TEN);
        account.reserve(new BigDecimal("4"));
        account.release(new BigDecimal("2"));
        account.freeze(new BigDecimal("3"));
        account.unfreeze(BigDecimal.ONE);
        account.outbound(new BigDecimal("2"));

        assertThat(account.onHandQty()).isEqualByComparingTo("8");
        assertThat(account.availableQty()).isEqualByComparingTo("6");
        assertThat(account.reservedQty()).isZero();
        assertThat(account.frozenQty()).isEqualByComparingTo("2");
    }

    @Test
    void cannotReserveMoreThanAvailableOrAdjustBelowZero() {
        var account = new InventoryAccountAggregate(1, 1, 1, "SKU", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

        assertThatThrownBy(() -> account.reserve(BigDecimal.ONE)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> account.adjust(BigDecimal.ONE.negate())).isInstanceOf(BusinessException.class);
    }
}
