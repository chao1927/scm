package com.chaobo.scm.purchase.application.price;

import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.InMemoryIdempotencyPort;
import com.chaobo.scm.purchase.domain.price.PurchasePriceRepository;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;


class PurchasePriceApplicationServiceTest {

    @Test
    void overlapCheckIncludesPriceTypeAsApplicableScope() {
        var repository = new CapturingRepository();
        IdentifierGenerator ids = new IdentifierGenerator() {
            @Override
            public long nextId() {
                return 1;
            }

            @Override
            public String nextCode(String prefix) {
                return prefix + "001";
            }
        };
        var service = new PurchasePriceApplicationService(repository, events -> { }, (context, operation, aggregateType, aggregateId, aggregateNo, before, after) -> { }, ids, new InMemoryIdempotencyPort());
        var command = new PurchasePriceCommands.Create(3001, "SKU-01", 2001, 2, "CNY", new BigDecimal("10.00"), new BigDecimal("0.13"), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), "MANUAL", "M-001");

        service.create(command, context());

        org.assertj.core.api.Assertions.assertThat(repository.priceType).isEqualTo(2);
    }

    private CommandContext context() {
        return new CommandContext(1001, "buyer", 1, 2001L, "REQ-PRICE-001", "TRACE-001",
                "IDEM-PRICE-001", Set.of("purchase:price:create"), "price-request-digest");
    }

    private static final class CapturingRepository implements PurchasePriceRepository {
        private int priceType;

        @Override
        public Optional<com.chaobo.scm.purchase.domain.price.PurchasePriceAggregate> findByNo(String priceNo) {
            return Optional.empty();
        }

        @Override
        public List<com.chaobo.scm.purchase.domain.price.PurchasePriceAggregate> findActiveOverlaps(long supplierId, String skuCode, long purchaseOrgId, int priceType, String currency, LocalDate effectiveFrom, LocalDate effectiveTo) {
            this.priceType = priceType;
            return List.of();
        }

        @Override
        public void save(com.chaobo.scm.purchase.domain.price.PurchasePriceAggregate aggregate, long operatorId) {
        }
    }
}
