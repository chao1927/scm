package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CancellationRequestAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class CancellationRequestAggregateTest {

    /**
     * 执行命令 {@code cancellationWaitsForWmsAndStockRelease}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void cancellationWaitsForWmsAndStockRelease() {
        CancellationRequestAggregate aggregate = CancellationRequestAggregate.create("CAN-1", "SO-1", "FUL-1", "OUT-1", "RES-1", "客户不需要");
        aggregate.approve("客服同意");
        aggregate.process(true);
        aggregate.markWmsCancelled();
        aggregate.markStockReleased();
        assertThat(aggregate.status()).isEqualTo(CancellationRequestAggregate.COMPLETED);
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType).containsExactly("CancelRequestCreated", "CancelRequestApproved", "WmsCancelRequested", "StockReleaseRequested", "SalesOrderCanceled");
    }

    /**
     * 处理当前类型职责中的操作 {@code shippedOrderCannotBeCompletedByCancellation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shippedOrderCannotBeCompletedByCancellation() {
        CancellationRequestAggregate aggregate = CancellationRequestAggregate.create("CAN-1", "SO-1", "FUL-1", "OUT-1", "RES-1", "客户不需要");
        aggregate.approve("客服同意");
        aggregate.process(true);
        assertThatThrownBy(() -> aggregate.markStockReleased()).isInstanceOf(IllegalStateException.class).hasMessageContaining("WMS");
    }
}
