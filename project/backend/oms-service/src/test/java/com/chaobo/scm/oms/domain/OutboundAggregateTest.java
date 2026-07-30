package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OutboundAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class OutboundAggregateTest {

    /**
     * 执行命令 {@code dispatchesAndTracksWmsAcceptance}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void dispatchesAndTracksWmsAcceptance() {
        OutboundAggregate aggregate = OutboundAggregate.create("OUT-1", "FUL-1", "SO-1", 100L, "WH-1");
        aggregate.dispatch();
        aggregate.markWmsAccepted("WMS-1");
        assertThat(aggregate.status()).isEqualTo(OutboundAggregate.WMS_ACCEPTED);
        assertThat(aggregate.wmsOrderNo()).isEqualTo("WMS-1");
        assertThat(aggregate.pullEvents()).extracting(OmsEvent::eventType).containsExactly("OutboundOrderCreated", "OutboundInstructionIssued", "WmsOutboundAccepted");
    }

    /**
     * 处理当前类型职责中的操作 {@code shippedOutboundCannotBeCancelled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shippedOutboundCannotBeCancelled() {
        OutboundAggregate aggregate = OutboundAggregate.create("OUT-1", "FUL-1", "SO-1", 100L, "WH-1");
        aggregate.dispatch();
        aggregate.markWmsAccepted("WMS-1");
        aggregate.markShipped();
        assertThatThrownBy(() -> aggregate.requestCancel("客户取消")).isInstanceOf(IllegalStateException.class).hasMessageContaining("shipped");
    }
}
