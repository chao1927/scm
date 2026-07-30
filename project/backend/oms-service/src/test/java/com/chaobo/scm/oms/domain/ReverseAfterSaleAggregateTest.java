package com.chaobo.scm.oms.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ReverseAfterSaleAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ReverseAfterSaleAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code returnRefundRequiresInspectionBeforeRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void returnRefundRequiresInspectionBeforeRefund() {
        var afterSale = ReverseAfterSaleAggregate.create("AS-1", ReverseAfterSaleAggregate.Type.RETURN_REFUND, "SO-1", "FUL-1", 88, "SKU-1", new BigDecimal("2"), new BigDecimal("20"), 10, "质量问题");
        afterSale.approve("RMA-1", 0);
        assertThatThrownBy(() -> afterSale.requestRefund(1)).isInstanceOf(IllegalStateException.class);
        afterSale.inspect(new BigDecimal("2"), new BigDecimal("1"), false, 1);
        afterSale.requestRefund(2);
        afterSale.markRefunded(new BigDecimal("10"), 3);
        assertThat(afterSale.status()).isEqualTo(ReverseAfterSaleAggregate.COMPLETED);
        assertThat(afterSale.acceptedQty()).isEqualByComparingTo("1");
    }

    /**
     * 执行命令 {@code rejectsInspectionQuantityAboveAppliedQuantity}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectsInspectionQuantityAboveAppliedQuantity() {
        var afterSale = ReverseAfterSaleAggregate.create("AS-2", ReverseAfterSaleAggregate.Type.EXCHANGE, "SO-1", "FUL-1", 88, "SKU-1", BigDecimal.ONE, BigDecimal.ZERO, 10, "破损");
        afterSale.approve("RMA-2", 0);
        assertThatThrownBy(() -> afterSale.inspect(new BigDecimal("2"), BigDecimal.ONE, false, 1)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("quantities");
    }
}
