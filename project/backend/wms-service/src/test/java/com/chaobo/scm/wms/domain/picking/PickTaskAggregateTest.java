package com.chaobo.scm.wms.domain.picking;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PickTaskAggregateTest {
    @Test
    void pickCannotExceedRequired() {
        var task = new PickTaskAggregate(1, "PK1", 1, 1, "SKU", BigDecimal.TEN, BigDecimal.ZERO, 1, 0);

        task.pick(new BigDecimal("5"));

        assertThatThrownBy(() -> task.pick(new BigDecimal("6"))).isInstanceOf(BusinessException.class);
        task.pick(new BigDecimal("5"));
        assertThat(task.status()).isEqualTo(3);
    }
}
