package com.chaobo.scm.purchase.domain.supplierreturn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SupplierReturnAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierReturnAggregateTest {

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * 处理当前类型职责中的操作 {@code lineRejectsReturnQuantityGreaterThanReturnableQuantity}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void lineRejectsReturnQuantityGreaterThanReturnableQuantity() {
        assertThatThrownBy(() -> new SupplierReturnLine(1, "SKU-01", new BigDecimal("6"), new BigDecimal("5"), "质检不合格")).isInstanceOf(BusinessException.class).hasMessageContaining("不能超过可退数量");
    }

    /**
     * 执行命令 {@code submitApproveAndNotifyExecutionChangesStatusAndRaisesEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void submitApproveAndNotifyExecutionChangesStatusAndRaisesEvents() {
        var aggregate = supplierReturn();
        aggregate.pullEvents();
        aggregate.submit(ids);
        aggregate.approve(true, null, ids);
        aggregate.notifyExecution("EVENT", ids);
        assertThat(aggregate.status()).isEqualTo(SupplierReturnStatus.EXECUTION_NOTIFIED);
        assertThat(aggregate.version()).isEqualTo(3);
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("SupplierReturnSubmitted", "SupplierReturnApproved", "SupplierReturnExecutionNotified");
    }

    /**
     * 执行命令 {@code rejectStoresReasonAndStopsAtRejected}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectStoresReasonAndStopsAtRejected() {
        var aggregate = supplierReturn();
        aggregate.pullEvents();
        aggregate.submit(ids);
        aggregate.approve(false, "证据不足", ids);
        assertThat(aggregate.status()).isEqualTo(SupplierReturnStatus.REJECTED);
        assertThat(aggregate.rejectReason()).isEqualTo("证据不足");
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("SupplierReturnSubmitted", "SupplierReturnRejected");
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierReturn}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnAggregate}
     */
    private SupplierReturnAggregate supplierReturn() {
        return SupplierReturnAggregate.create("PO001", 3001, 2001, "WH001", List.of(new SupplierReturnLine(ids.nextId(), "SKU-01", new BigDecimal("5"), new BigDecimal("10"), "质检不合格")), ids);
    }

    /**
     * TestIdentifierGenerator。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
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
        private final AtomicLong sequence = new AtomicLong(8000);

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
