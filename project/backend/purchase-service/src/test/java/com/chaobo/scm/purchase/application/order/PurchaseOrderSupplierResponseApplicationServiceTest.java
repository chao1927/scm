package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.InMemoryIdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderAggregate;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderLine;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderRepository;
import com.chaobo.scm.purchase.domain.order.PurchaseOrderStatus;
import com.chaobo.scm.purchase.domain.inbound.InboundTrackingRepository;
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

/**
 * PurchaseOrderSupplierResponseApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PurchaseOrderSupplierResponseApplicationServiceTest {

    /**
     * ids（类型：{@code TestIdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final TestIdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * repository（类型：{@code InMemoryOrderRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();

    /**
     * outboxEvents（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> outboxEvents = new ArrayList<>();

    /**
     * service（类型：{@code PurchaseOrderApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderApplicationService service = new PurchaseOrderApplicationService(repository, outboxEvents::addAll, noAudit(), ids, new InMemoryIdempotencyPort(), null, emptyInbounds());

    private static InboundTrackingRepository emptyInbounds() {
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
                return false;
            }

            @Override
            public void save(com.chaobo.scm.purchase.domain.inbound.InboundTrackingAggregate aggregate, long operatorId) {
            }
        };
    }

    /**
     * 执行命令 {@code confirmationEventChangesOrderAndWritesLocalDomainEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void confirmationEventChangesOrderAndWritesLocalDomainEvent() {
        var order = releasedOrder();
        repository.aggregate = order;
        var result = service.recordSupplierResponse(order.orderNo(), order.supplierId(), PurchaseOrderApplicationService.SupplierResponseType.CONFIRMED, "已确认", systemContext());
        assertThat(result.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_CONFIRMED.code());
        assertThat(repository.aggregate.status()).isEqualTo(PurchaseOrderStatus.SUPPLIER_CONFIRMED);
        assertThat(outboxEvents).extracting(DomainEvent::eventType).contains("SupplierOrderConfirmationRecorded");
    }

    /**
     * 执行命令 {@code releasedOrder}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 执行命令的结果，类型为 {@code PurchaseOrderAggregate}
     */
    private PurchaseOrderAggregate releasedOrder() {
        var order = PurchaseOrderAggregate.create(1, 3001, "SUP001", "测试供应商", 2001, "WH001", "CNY", List.of(new PurchaseOrderLine(ids.nextId(), "SKU-01", "测试SKU", BigDecimal.TEN, new BigDecimal("12"), new BigDecimal("0.13"), null, LocalDate.now().plusDays(7), BigDecimal.ZERO)), ids);
        order.pullEvents();
        order.submit(ids);
        order.approve(true, null, ids);
        order.publish("EVENT", ids);
        order.pullEvents();
        return order;
    }

    /**
     * 处理当前类型职责中的操作 {@code systemContext}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandContext}
     */
    private static CommandContext systemContext() {
        return CommandContext.forEvent("SUPPLIER", "event-1", null, java.util.Set.of(), "{}");
    }

    /**
     * 处理当前类型职责中的操作 {@code noAudit}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AuditLogRepository}
     */
    private static AuditLogRepository noAudit() {
        return (context, operation, targetType, targetId, targetNo, before, after) -> {
        };
    }

    /**
     * InMemoryOrderRepository。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class InMemoryOrderRepository implements PurchaseOrderRepository {

        /**
         * aggregate（类型：{@code PurchaseOrderAggregate}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private PurchaseOrderAggregate aggregate;

        /**
         * 查询并返回 {@code findByNo}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param orderNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Optional<PurchaseOrderAggregate>}
         */
        @Override
        public Optional<PurchaseOrderAggregate> findByNo(String orderNo) {
            return aggregate != null && aggregate.orderNo().equals(orderNo) ? Optional.of(aggregate) : Optional.empty();
        }

        /**
         * 执行命令 {@code save}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param aggregate 业务处理参数或成员，类型为 {@code PurchaseOrderAggregate}
         * @param operatorId 业务或技术标识，类型为 {@code long}
         */
        @Override
        public void save(PurchaseOrderAggregate aggregate, long operatorId) {
            this.aggregate = aggregate;
        }
    }

    /**
     * TestIdentifierGenerator。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class TestIdentifierGenerator implements IdentifierGenerator {

        /**
         * sequence（类型：{@code AtomicLong}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final AtomicLong sequence = new AtomicLong(7000);

        /**
         * 处理当前类型职责中的操作 {@code nextId}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        @Override
        public long nextId() {
            return sequence.incrementAndGet();
        }

        /**
         * 处理当前类型职责中的操作 {@code nextCode}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param prefix 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        @Override
        public String nextCode(String prefix) {
            return prefix + sequence.incrementAndGet();
        }
    }
}
