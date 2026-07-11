package com.chaobo.scm.purchase.domain.supplierreturn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierReturnAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void lineRejectsReturnQuantityGreaterThanReturnableQuantity() {
        assertThatThrownBy(() -> new SupplierReturnLine(1, "SKU-01", new BigDecimal("6"),
                new BigDecimal("5"), "质检不合格"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能超过可退数量");
    }

    @Test
    void submitApproveAndNotifyExecutionChangesStatusAndRaisesEvents() {
        var aggregate = supplierReturn();
        aggregate.pullEvents();

        aggregate.submit(ids);
        aggregate.approve(true, null, ids);
        aggregate.notifyExecution("EVENT", ids);

        assertThat(aggregate.status()).isEqualTo(SupplierReturnStatus.EXECUTION_NOTIFIED);
        assertThat(aggregate.version()).isEqualTo(3);
        assertThat(aggregate.pullEvents()).extracting("eventType")
                .containsExactly("SupplierReturnSubmitted", "SupplierReturnApproved",
                        "SupplierReturnExecutionNotified");
    }

    @Test
    void rejectStoresReasonAndStopsAtRejected() {
        var aggregate = supplierReturn();
        aggregate.pullEvents();

        aggregate.submit(ids);
        aggregate.approve(false, "证据不足", ids);

        assertThat(aggregate.status()).isEqualTo(SupplierReturnStatus.REJECTED);
        assertThat(aggregate.rejectReason()).isEqualTo("证据不足");
        assertThat(aggregate.pullEvents()).extracting("eventType")
                .containsExactly("SupplierReturnSubmitted", "SupplierReturnRejected");
    }

    private SupplierReturnAggregate supplierReturn() {
        return SupplierReturnAggregate.create("PO001", 3001, 2001, "WH001",
                List.of(new SupplierReturnLine(ids.nextId(), "SKU-01", new BigDecimal("5"),
                        new BigDecimal("10"), "质检不合格")), ids);
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(8000);

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
