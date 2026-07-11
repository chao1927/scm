package com.chaobo.scm.purchase.domain.price;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchasePriceAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void createCalculatesTaxIncludedPriceAndRaisesEvent() {
        var price = PurchasePriceAggregate.create(
                3001,
                "SKU-01",
                2001,
                2,
                "CNY",
                new BigDecimal("10"),
                new BigDecimal("0.13"),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "BID_COMPARISON",
                "CMP001",
                ids);

        assertThat(price.taxIncludedPrice()).isEqualByComparingTo("11.300000");
        assertThat(price.status()).isEqualTo(PurchasePriceStatus.ACTIVE);
        assertThat(price.pullEvents()).extracting("eventType").containsExactly("PurchasePriceActivated");
    }

    @Test
    void invalidEffectiveRangeIsRejected() {
        assertThatThrownBy(() -> PurchasePriceAggregate.create(
                3001,
                "SKU-01",
                2001,
                2,
                "CNY",
                new BigDecimal("10"),
                new BigDecimal("0.13"),
                LocalDate.now().plusDays(10),
                LocalDate.now(),
                "MANUAL",
                "M001",
                ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("有效期不合法");
    }

    @Test
    void disableChangesStatusAndRaisesEvent() {
        var price = PurchasePriceAggregate.create(
                3001,
                "SKU-01",
                2001,
                2,
                "CNY",
                new BigDecimal("10"),
                new BigDecimal("0.13"),
                LocalDate.now(),
                null,
                "MANUAL",
                "M001",
                ids);
        price.pullEvents();

        price.disable(ids);

        assertThat(price.status()).isEqualTo(PurchasePriceStatus.DISABLED);
        assertThat(price.pullEvents()).extracting("eventType").containsExactly("PurchasePriceDisabled");
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(4000);

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
