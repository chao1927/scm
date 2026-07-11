package com.chaobo.scm.purchase.domain.orderchange;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseOrderChangeAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void approveMakesChangeEffective() {
        var change = PurchaseOrderChangeAggregate.create("PO001", 1, "{\"qty\":10}", "{\"qty\":20}",
                "供应商要求调整", ids);
        change.pullEvents();

        change.approve(true, ids);

        assertThat(change.status()).isEqualTo(PurchaseOrderChangeStatus.EFFECTIVE);
        assertThat(change.pullEvents()).extracting("eventType").containsExactly("PurchaseOrderChangeEffective");
    }

    @Test
    void cannotApproveTwice() {
        var change = PurchaseOrderChangeAggregate.create("PO001", 1, "{\"qty\":10}", "{\"qty\":20}",
                "供应商要求调整", ids);
        change.approve(true, ids);

        assertThatThrownBy(() -> change.approve(true, ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能审批");
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(6000);

        @Override
        public long nextId() {
            return sequence.incrementAndGet();
        }

        @Override
        public String nextCode(String prefix) {
            return prefix + sequence.incrementAndGet();
        }
    }
}
