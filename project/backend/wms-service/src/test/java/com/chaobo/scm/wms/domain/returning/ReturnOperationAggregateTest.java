package com.chaobo.scm.wms.domain.returning;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

/**
 * ReturnOperationAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ReturnOperationAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code protectsFiveWayDispositionConservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void protectsFiveWayDispositionConservation() {
        var a = new ReturnOperationAggregate(1, "AS-1", "RMA-1", 88, 10, "SKU-1", null, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, ReturnOperationAggregate.RECEIVING, 0);
        a.receive(new BigDecimal("5"), 0);
        a.inspect(new BigDecimal("2"), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, 1);
        assertThat(a.status()).isEqualTo(ReturnOperationAggregate.COMPLETED);
        assertThatThrownBy(() -> a.inspect(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 2)).isInstanceOf(RuntimeException.class);
    }
}
