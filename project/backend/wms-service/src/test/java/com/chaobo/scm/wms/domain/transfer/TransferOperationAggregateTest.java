package com.chaobo.scm.wms.domain.transfer;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TransferOperationAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class TransferOperationAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code protectsOutboundAndReceiptQuantities}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void protectsOutboundAndReceiptQuantities() {
        var transfer = new TransferOperationAggregate(1, "TRF-1", 1, 10, 20, "SKU-1", null, new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, TransferOperationAggregate.OUTBOUND_PENDING, 0);
        transfer.completeOutbound(new BigDecimal("5"), 0);
        transfer.prepareInbound(1);
        transfer.receive(new BigDecimal("4"), true, 2);
        assertThat(transfer.status()).isEqualTo(TransferOperationAggregate.RECEIVED);
        assertThat(transfer.receivedQty()).isEqualByComparingTo("4");
        assertThatThrownBy(() -> transfer.receive(BigDecimal.ONE, true, 3)).isInstanceOf(BusinessException.class);
    }
}
