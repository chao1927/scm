package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AfterSaleAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class AfterSaleAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code requestsAndCompletesRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void requestsAndCompletesRefund() {
        AfterSaleAggregate aggregate = AfterSaleAggregate.create("AS-1", "SO-1", "FUL-1", new BigDecimal("20.00"), "仅退款");
        aggregate.approve("符合规则");
        aggregate.requestRefund();
        aggregate.markRefunded(new BigDecimal("20.00"));
        aggregate.complete();
        assertThat(aggregate.status()).isEqualTo(AfterSaleAggregate.COMPLETED);
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType).containsExactly("AfterSaleCreated", "AfterSaleApproved", "RefundRequested", "RefundCompleted", "AfterSaleCompleted");
    }

    /**
     * 处理当前类型职责中的操作 {@code refundCannotExceedRequestedAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void refundCannotExceedRequestedAmount() {
        AfterSaleAggregate aggregate = AfterSaleAggregate.create("AS-1", "SO-1", "FUL-1", new BigDecimal("20.00"), "仅退款");
        aggregate.approve("符合规则");
        aggregate.requestRefund();
        assertThatThrownBy(() -> aggregate.markRefunded(new BigDecimal("20.01"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("refund amount");
    }
}
