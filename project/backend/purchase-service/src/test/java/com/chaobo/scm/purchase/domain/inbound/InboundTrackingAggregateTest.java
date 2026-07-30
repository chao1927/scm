package com.chaobo.scm.purchase.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InboundTrackingAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class InboundTrackingAggregateTest {

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids = new TestIdentifierGenerator();

    /**
     * 处理当前类型职责中的操作 {@code recordAsnCreatesTrackingAndEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void recordAsnCreatesTrackingAndEvent() {
        var aggregate = inbound();
        assertThat(aggregate.status()).isEqualTo(InboundStatus.ASN_RECORDED);
        assertThat(aggregate.receivedQty()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("PurchaseAsnRecorded");
    }

    /**
     * 处理当前类型职责中的操作 {@code syncWmsMovesToPutawayAndRaisesEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void syncWmsMovesToPutawayAndRaisesEvent() {
        var aggregate = inbound();
        aggregate.pullEvents();
        aggregate.syncWms(new BigDecimal("10"), new BigDecimal("8"), new BigDecimal("2"), new BigDecimal("8"), "WMS回传", ids);
        assertThat(aggregate.status()).isEqualTo(InboundStatus.PUTAWAY);
        assertThat(aggregate.version()).isEqualTo(1);
        assertThat(aggregate.qualifiedQty()).isEqualByComparingTo("8");
        assertThat(aggregate.unqualifiedQty()).isEqualByComparingTo("2");
        assertThat(aggregate.pullEvents()).extracting("eventType").containsExactly("PurchaseGoodsPutawayCompleted");
    }

    /**
     * 处理当前类型职责中的操作 {@code syncWmsRejectsReceivedQuantityGreaterThanAsn}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void syncWmsRejectsReceivedQuantityGreaterThanAsn() {
        var aggregate = inbound();
        assertThatThrownBy(() -> aggregate.syncWms(new BigDecimal("11"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "超收", ids)).isInstanceOf(BusinessException.class).hasMessageContaining("超过ASN通知数量");
    }

    /**
     * 处理当前类型职责中的操作 {@code inbound}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InboundTrackingAggregate}
     */
    private InboundTrackingAggregate inbound() {
        return InboundTrackingAggregate.recordAsn("PO001", "ASN001", 3001, 2001, "WH001", "SKU-01", new BigDecimal("10"), ids);
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
