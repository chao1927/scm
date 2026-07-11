package com.chaobo.scm.purchase.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundTrackingAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void recordAsnCreatesTrackingAndEvent() {
        var aggregate = inbound();

        assertThat(aggregate.status()).isEqualTo(InboundStatus.ASN_RECORDED);
        assertThat(aggregate.receivedQty()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("PurchaseAsnRecorded");
    }

    @Test
    void syncWmsMovesToPutawayAndRaisesEvent() {
        var aggregate = inbound();
        aggregate.pullEvents();

        aggregate.syncWms(new BigDecimal("10"), new BigDecimal("8"), new BigDecimal("2"),
                new BigDecimal("8"), "WMS回传", ids);

        assertThat(aggregate.status()).isEqualTo(InboundStatus.PUTAWAY);
        assertThat(aggregate.version()).isEqualTo(1);
        assertThat(aggregate.qualifiedQty()).isEqualByComparingTo("8");
        assertThat(aggregate.unqualifiedQty()).isEqualByComparingTo("2");
        assertThat(aggregate.pullEvents()).extracting("eventType")
                .containsExactly("PurchaseGoodsPutawayCompleted");
    }

    @Test
    void syncWmsRejectsReceivedQuantityGreaterThanAsn() {
        var aggregate = inbound();

        assertThatThrownBy(() -> aggregate.syncWms(new BigDecimal("11"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, "超收", ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("超过ASN通知数量");
    }

    private InboundTrackingAggregate inbound() {
        return InboundTrackingAggregate.recordAsn("PO001", "ASN001", 3001, 2001, "WH001",
                "SKU-01", new BigDecimal("10"), ids);
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(7000);

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
