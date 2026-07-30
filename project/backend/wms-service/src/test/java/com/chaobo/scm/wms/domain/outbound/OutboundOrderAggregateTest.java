package com.chaobo.scm.wms.domain.outbound;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OutboundOrderAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class OutboundOrderAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code allocationAndCancellationFollowStateMachine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void allocationAndCancellationFollowStateMachine() {
        var outbound = new OutboundOrderAggregate(1, "OUT1", "OMS", "SO1", 1, 1, 0);
        outbound.allocate();
        assertThat(outbound.status()).isEqualTo(2);
        outbound.cancel("客户取消");
        assertThat(outbound.status()).isEqualTo(9);
        assertThatThrownBy(outbound::allocate).isInstanceOf(BusinessException.class);
    }

    /**
     * 执行命令 {@code cancelRequiresReasonAndRejectsRepeatedCancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void cancelRequiresReasonAndRejectsRepeatedCancel() {
        var outbound = new OutboundOrderAggregate(1, "OUT1", "OMS", "SO1", 1, 1, 0);
        assertThatThrownBy(() -> outbound.cancel("")).isInstanceOf(BusinessException.class);
        outbound.cancel("客户取消");
        assertThatThrownBy(() -> outbound.cancel("重复取消")).isInstanceOf(BusinessException.class);
    }
}
