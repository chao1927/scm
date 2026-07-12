package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.InMemoryIdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderAggregate;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderLine;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderRepository;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderStatus;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseOrderSupplierResponseApplicationServiceTest {
    private final TestIdentifierGenerator ids = new TestIdentifierGenerator();
    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final List<DomainEvent> outboxEvents = new ArrayList<>();
    private final PurchaseOrderApplicationService service = new PurchaseOrderApplicationService(
            repository, outboxEvents::addAll, noAudit(), ids, new InMemoryIdempotencyPort(), null);

    @Test
    void confirmationEventChangesOrderAndWritesLocalDomainEvent() {
        var order = releasedOrder();
        repository.aggregate = order;

        var result = service.recordSupplierResponse(order.orderNo(), order.supplierId(),
                PurchaseOrderApplicationService.SupplierResponseType.CONFIRMED, "已确认", systemContext());

        assertThat(result.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_CONFIRMED.code());
        assertThat(repository.aggregate.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_CONFIRMED);
        assertThat(outboxEvents).extracting(DomainEvent::eventType)
                .contains("SupplierOrderConfirmationRecorded");
    }

    private PurchaseOrderAggregate releasedOrder() {
        var order = PurchaseOrderAggregate.create(1, 3001, "SUP001", "测试供应商", 2001, "WH001", "CNY",
                List.of(new PurchaseOrderLine(ids.nextId(), "SKU-01", "测试SKU", BigDecimal.TEN,
                        new BigDecimal("12"), new BigDecimal("0.13"), null, LocalDate.now().plusDays(7),
                        BigDecimal.ZERO)), ids);
        order.pullEvents();
        order.submit(ids);
        order.approve(true, null, ids);
        order.publish("EVENT", ids);
        order.pullEvents();
        return order;
    }

    private static CommandContext systemContext() {
        return new CommandContext(0, "SUPPLIER", 0, null, "event-1", null, "SUPPLIER:event-1", java.util.Set.of());
    }

    private static AuditLogRepository noAudit() {
        return (context, operation, targetType, targetId, targetNo, before, after) -> { };
    }

    private static final class InMemoryOrderRepository implements PurchaseOrderRepository {
        private PurchaseOrderAggregate aggregate;

        @Override
        public Optional<PurchaseOrderAggregate> findByNo(String orderNo) {
            return aggregate != null && aggregate.orderNo().equals(orderNo) ? Optional.of(aggregate) : Optional.empty();
        }

        @Override
        public void save(PurchaseOrderAggregate aggregate, long operatorId) {
            this.aggregate = aggregate;
        }
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
