package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsnAggregateTest {
    private final TestIdentifierGenerator generator = new TestIdentifierGenerator();

    @Test
    void shouldCreateDraftAndRaiseEvent() {
        AsnAggregate aggregate = createDraft();

        assertThat(aggregate.status()).isEqualTo(AsnStatus.DRAFT);
        assertThat(aggregate.lines()).hasSize(1);
        assertThat(aggregate.pullEvents()).extracting(event -> event.eventType())
                .containsExactly("SupplierAsnCreated");
    }

    @Test
    void shouldSubmitThenShip() {
        AsnAggregate aggregate = createDraft();
        aggregate.pullEvents();

        aggregate.submit(1001L, generator);
        aggregate.confirmShipment(new ShipmentInfo(OffsetDateTime.now(), "顺丰供应链", "SF10001"),
                1001L, generator);

        assertThat(aggregate.status()).isEqualTo(AsnStatus.SHIPPED);
        assertThat(aggregate.version()).isEqualTo(2);
        assertThat(aggregate.pullEvents()).extracting(event -> event.eventType())
                .containsExactly("SupplierAsnSubmitted", "SupplierAsnShipped");
    }

    @Test
    void shouldRejectShippingDraft() {
        AsnAggregate aggregate = createDraft();

        assertThatThrownBy(() -> aggregate.confirmShipment(
                new ShipmentInfo(OffsetDateTime.now(), "顺丰供应链", "SF10001"), 1001L, generator))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ErrorCode.STATE_CONFLICT));
    }

    private AsnAggregate createDraft() {
        return AsnAggregate.create(2001L, 3001L, 4001L, OffsetDateTime.now().plusDays(1),
                List.of(new AsnAggregate.NewLine("SKU-001", new BigDecimal("10"),
                        "BATCH-01", null, null)), 1001L, generator);
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(1);

        @Override
        public long nextId() {
            return sequence.getAndIncrement();
        }

        @Override
        public String nextBusinessNo(String prefix) {
            return prefix + sequence.getAndIncrement();
        }
    }
}
