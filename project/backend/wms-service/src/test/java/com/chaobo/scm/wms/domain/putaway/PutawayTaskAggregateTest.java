package com.chaobo.scm.wms.domain.putaway;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PutawayTaskAggregateTest {
    @Test
    void putawayCannotExceedQualifiedQuantity() {
        var task = new PutawayTaskAggregate(1, "PUT001", 1, BigDecimal.TEN);
        task.putaway(new BigDecimal("6"), "A-01-01");
        assertThatThrownBy(() -> task.putaway(new BigDecimal("5"), "A-01-01"))
                .isInstanceOf(BusinessException.class);
        task.putaway(new BigDecimal("4"), "A-01-01");
        assertThat(task.completed()).isTrue();
    }
}
