package com.chaobo.scm.wms.domain.receiving;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ReceiptAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ReceiptAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code receiptRequiresBalancedQuantityBeforeCompletion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void receiptRequiresBalancedQuantityBeforeCompletion() {
        var receipt = new ReceiptAggregate(1, "REC001", 1, "SKU001", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, ReceiptStatus.RECEIVING, 0);
        receipt.scan(new BigDecimal("8"), BigDecimal.ZERO, null);
        assertThatThrownBy(receipt::complete).isInstanceOf(BusinessException.class);
        receipt.scan(new BigDecimal("2"), BigDecimal.ZERO, null);
        receipt.complete();
        assertThat(receipt.status()).isEqualTo(ReceiptStatus.COMPLETED);
    }

    /**
     * 执行命令 {@code rejectedQuantityRequiresReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectedQuantityRequiresReason() {
        var receipt = new ReceiptAggregate(1, "REC001", 1, "SKU001", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, ReceiptStatus.RECEIVING, 0);
        assertThatThrownBy(() -> receipt.scan(BigDecimal.ZERO, BigDecimal.ONE, null)).isInstanceOf(BusinessException.class).hasMessageContaining("拒收必须填写原因");
    }
}
