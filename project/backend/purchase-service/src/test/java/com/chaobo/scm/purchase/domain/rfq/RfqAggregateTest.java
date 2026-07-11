package com.chaobo.scm.purchase.domain.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RfqAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void createRejectsDuplicateSuppliers() {
        assertThatThrownBy(() -> RfqAggregate.create(
                2,
                2001,
                "CATE-01",
                "PR001",
                OffsetDateTime.now().plusDays(3),
                List.of(line("SKU-01")),
                List.of(invitation(3001), invitation(3001)),
                ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("邀请供应商不能重复");
    }

    @Test
    void publishRaisesEventForEachSupplierAndLocksStatus() {
        var aggregate = rfq();
        aggregate.pullEvents();

        aggregate.publish(ids);

        assertThat(aggregate.status()).isEqualTo(RfqStatus.QUOTING);
        assertThat(aggregate.publishedAt()).isNotNull();
        assertThat(aggregate.pullEvents())
                .extracting("eventType")
                .containsExactly("RfqPublished", "RfqPublished");
    }

    @Test
    void closeBiddingClosesSupplierTodos() {
        var aggregate = rfq();
        aggregate.publish(ids);
        aggregate.pullEvents();

        aggregate.closeBidding("到达报价截止时间", ids);

        assertThat(aggregate.status()).isEqualTo(RfqStatus.BIDDING_CLOSED);
        assertThat(aggregate.invitations()).allMatch(invitation -> invitation.quoteStatus() == 4);
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("RfqBiddingClosed");
    }

    @Test
    void cannotPublishTwice() {
        var aggregate = rfq();
        aggregate.publish(ids);

        assertThatThrownBy(() -> aggregate.publish(ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不允许");
    }

    private RfqAggregate rfq() {
        return RfqAggregate.create(
                2,
                2001,
                "CATE-01",
                "PR001",
                OffsetDateTime.now().plusDays(3),
                List.of(line("SKU-01")),
                List.of(invitation(3001), invitation(3002)),
                ids);
    }

    private RfqLine line(String skuCode) {
        return new RfqLine(
                ids.nextId(),
                skuCode,
                new BigDecimal("10"),
                "PCS",
                LocalDate.now().plusDays(7),
                "常规质检");
    }

    private RfqInvitation invitation(long supplierId) {
        return new RfqInvitation(ids.nextId(), supplierId, 1);
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(2000);

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
