package com.chaobo.scm.purchase.domain.comparison;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BidComparisonAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void generateRequiresAtLeastTwoCandidates() {
        assertThatThrownBy(() -> BidComparisonAggregate.generate(
                "RFQ001",
                2001,
                "CNY",
                List.of(candidate(3001, "Q001", "10")),
                ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("至少需要两个");
    }

    @Test
    void awardMarksOnlyOneCandidateAndRaisesEvent() {
        var aggregate = BidComparisonAggregate.generate(
                "RFQ001",
                2001,
                "CNY",
                List.of(candidate(3001, "Q001", "10"), candidate(3002, "Q002", "12")),
                ids);
        aggregate.pullEvents();
        var winnerId = aggregate.recommended().candidateId();

        var winner = aggregate.award(winnerId, "综合得分最高", 1001, ids);

        assertThat(aggregate.status()).isEqualTo(BidComparisonStatus.AWARDED);
        assertThat(aggregate.awardedCandidateId()).isEqualTo(winnerId);
        assertThat(winner.awarded()).isTrue();
        assertThat(aggregate.candidates().stream().filter(BidCandidate::awarded)).hasSize(1);
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("CompareResultAwarded");
    }

    @Test
    void cannotAwardNonCandidate() {
        var aggregate = BidComparisonAggregate.generate(
                "RFQ001",
                2001,
                "CNY",
                List.of(candidate(3001, "Q001", "10"), candidate(3002, "Q002", "12")),
                ids);

        assertThatThrownBy(() -> aggregate.award(9999, "非候选", 1001, ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不在比价池");
    }

    private BidCandidate candidate(long supplierId, String quoteNo, String unitPrice) {
        return new BidCandidate(
                ids.nextId(),
                supplierId,
                "供应商" + supplierId,
                quoteNo,
                "SKU-01",
                new BigDecimal("100"),
                new BigDecimal(unitPrice),
                new BigDecimal("0.13"),
                5,
                new BigDecimal("80"),
                new BigDecimal("75"),
                new BigDecimal("20"),
                false);
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(3000);

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
