package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SalesOrderAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SalesOrderAggregateTest {

    /**
     * 执行命令 {@code createSalesOrderCalculatesAmountAndProducesEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createSalesOrderCalculatesAmountAndProducesEvents() {
        SalesOrderAggregate aggregate = SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市", List.of(new SalesOrderAggregate.OrderLine("SKU1", 2, new BigDecimal("12.50"))));
        assertThat(aggregate.totalAmount()).isEqualByComparingTo("25.00");
        assertThat(aggregate.status()).isEqualTo(SalesOrderAggregate.PENDING_REVIEW);
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType).containsExactly("ChannelOrderReceived", "SalesOrderCreated");
    }

    /**
     * 执行命令 {@code rejectInvalidLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectInvalidLines() {
        assertThatThrownBy(() -> SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市", List.of(new SalesOrderAggregate.OrderLine("SKU1", 0, BigDecimal.ONE)))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("invalid order line");
    }

    /**
     * 处理当前类型职责中的操作 {@code reviewStateMachineRejectsDuplicateReview}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void reviewStateMachineRejectsDuplicateReview() {
        SalesOrderAggregate aggregate = SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市", List.of(new SalesOrderAggregate.OrderLine("SKU1", 1, BigDecimal.ONE)));
        aggregate.approve("ok");
        assertThat(aggregate.status()).isEqualTo(SalesOrderAggregate.APPROVED);
        assertThatThrownBy(() -> aggregate.intercept("risk")).isInstanceOf(IllegalStateException.class).hasMessageContaining("not pending review");
    }

    /**
     * 处理当前类型职责中的操作 {@code interceptRequiresReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void interceptRequiresReason() {
        SalesOrderAggregate aggregate = SalesOrderAggregate.create("SO1", "TMALL", "C1001", 88L, "上海市", List.of(new SalesOrderAggregate.OrderLine("SKU1", 1, BigDecimal.ONE)));
        assertThatThrownBy(() -> aggregate.intercept("")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("reason");
    }
}
