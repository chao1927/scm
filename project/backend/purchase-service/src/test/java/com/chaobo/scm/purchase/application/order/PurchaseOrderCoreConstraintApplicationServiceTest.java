package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.InMemoryIdempotencyPort;
import com.chaobo.scm.purchase.domain.inbound.InboundTrackingRepository;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderAggregate;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderLine;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderRepository;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderStatus;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseOrderCoreConstraintApplicationServiceTest {

    @Test
    void cancelRejectsOrderWhenAsnAlreadyExists() {
        var repository = new FakeOrderRepository(order(0));
        var inbounds = inboundRepository(true);
        var service = service(repository, inbounds);

        assertThatThrownBy(() -> service.cancel("PO001", new PurchaseOrderCommands.Cancel(0, "需求取消"), context("purchase:po:cancel")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ASN");
    }

    @Test
    void applyChangeRejectsStalePurchaseOrderVersion() {
        var repository = new FakeOrderRepository(order(2));
        var inbounds = inboundRepository(false);
        var service = service(repository, inbounds);

        assertThatThrownBy(() -> service.applyChange("PO001", 1, Map.of(11L, new BigDecimal("12")), context("purchase:po-change:approve")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("版本");
    }

    private PurchaseOrderApplicationService service(PurchaseOrderRepository repository, InboundTrackingRepository inbounds) {
        return new PurchaseOrderApplicationService(repository, events -> { }, (context, operation, aggregateType, aggregateId, aggregateNo, before, after) -> { }, ids(), new InMemoryIdempotencyPort(), null, inbounds);
    }

    private PurchaseOrderAggregate order(int version) {
        var line = new PurchaseOrderLine(11, "SKU-01", "商品", new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("0.13"), null, LocalDate.of(2026, 8, 31), BigDecimal.ZERO);
        return new PurchaseOrderAggregate(1, "PO001", 1, 3001, "SUP-001", "供应商", 2001, "WH-001", "CNY", PurchaseOrderStatus.APPROVED, 1, version, null, null, List.of(line));
    }

    private IdentifierGenerator ids() {
        return new IdentifierGenerator() {
            @Override
            public long nextId() {
                return 99;
            }

            @Override
            public String nextCode(String prefix) {
                return prefix + "099";
            }
        };
    }

    private CommandContext context(String permission) {
        return new CommandContext(1001, "buyer", 1, 2001L, "REQ-PO-001", "TRACE-001", "IDEM-PO-001", Set.of(permission));
    }

    private InboundTrackingRepository inboundRepository(boolean exists) {
        return new InboundTrackingRepository() {
            @Override
            public Optional<com.chaobo.scm.purchase.domain.inbound.InboundTrackingAggregate> findByNo(String inboundNo) {
                return Optional.empty();
            }

            @Override
            public Optional<com.chaobo.scm.purchase.domain.inbound.InboundTrackingAggregate> findByAsnNo(String asnNo) {
                return Optional.empty();
            }

            @Override
            public boolean existsByOrderNo(String orderNo) {
                return exists;
            }

            @Override
            public void save(com.chaobo.scm.purchase.domain.inbound.InboundTrackingAggregate aggregate, long operatorId) {
            }
        };
    }

    private static final class FakeOrderRepository implements PurchaseOrderRepository {
        private final PurchaseOrderAggregate order;

        private FakeOrderRepository(PurchaseOrderAggregate order) {
            this.order = order;
        }

        @Override
        public Optional<PurchaseOrderAggregate> findByNo(String orderNo) {
            return Optional.of(order);
        }

        @Override
        public void save(PurchaseOrderAggregate aggregate, long operatorId) {
        }
    }
}
