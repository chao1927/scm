package com.chaobo.scm.purchase.domain.requisition;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseRequisitionAggregateTest {
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    @Test
    void createRejectsDuplicateSkuAndDate() {
        var requiredDate = LocalDate.now().plusDays(3);

        assertThatThrownBy(() -> PurchaseRequisitionAggregate.create(
                1001,
                2001,
                3001,
                "补货",
                List.of(
                        line("SKU-01", requiredDate, "10"),
                        line("SKU-01", requiredDate, "5")),
                ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能重复");
    }

    @Test
    void submitApproveAndConvertChangesStatusAndRaisesEvents() {
        var aggregate = PurchaseRequisitionAggregate.create(
                1001,
                2001,
                3001,
                "安全库存补货",
                List.of(line("SKU-01", LocalDate.now().plusDays(3), "10")),
                ids);
        aggregate.pullEvents();

        aggregate.submit(ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.SUBMITTED);

        var lineId = aggregate.lines().getFirst().lineId();
        aggregate.approve(Map.of(lineId, new BigDecimal("8")), ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.APPROVED);

        aggregate.convert(Map.of(lineId, new BigDecimal("3")), "RFQ", "RFQ20260711001", ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.PARTIALLY_CONVERTED);

        aggregate.convert(Map.of(lineId, new BigDecimal("5")), "RFQ", "RFQ20260711001", ids);
        assertThat(aggregate.status()).isEqualTo(PurchaseRequisitionStatus.CONVERTED);
        assertThat(aggregate.pullEvents()).extracting("eventType")
                .contains("PurchaseRequisitionSubmitted",
                        "PurchaseRequisitionApproved",
                        "PurchaseRequisitionConverted");
    }

    @Test
    void convertCannotExceedApprovedQuantity() {
        var aggregate = PurchaseRequisitionAggregate.create(
                1001,
                2001,
                3001,
                "安全库存补货",
                List.of(line("SKU-01", LocalDate.now().plusDays(3), "10")),
                ids);
        aggregate.submit(ids);
        var lineId = aggregate.lines().getFirst().lineId();
        aggregate.approve(Map.of(lineId, new BigDecimal("8")), ids);

        assertThatThrownBy(() -> aggregate.convert(
                Map.of(lineId, new BigDecimal("9")),
                "PO",
                "PO20260711001",
                ids))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能超过");
    }

    private PurchaseRequisitionLine line(String skuCode, LocalDate requiredDate, String quantity) {
        return new PurchaseRequisitionLine(
                ids.nextId(),
                skuCode,
                new BigDecimal(quantity),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "PCS",
                requiredDate,
                null);
    }

    private static final class TestIdentifierGenerator implements IdentifierGenerator {
        private final AtomicLong sequence = new AtomicLong(1000);

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
