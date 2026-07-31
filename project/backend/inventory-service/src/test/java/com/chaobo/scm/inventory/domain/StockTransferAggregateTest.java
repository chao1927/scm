package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * StockTransferAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class StockTransferAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code transferFollowsApprovalReservationTransitAndReceiptStateMachine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void transferFollowsApprovalReservationTransitAndReceiptStateMachine() {
        StockTransferAggregate transfer = transfer();
        transfer.submit(0);
        transfer.approve(1);
        transfer.reserve(new BigDecimal("10"), 2);
        transfer.recordOutbound(new BigDecimal("10"), 3);
        transfer.markInTransit(4);
        transfer.receive(new BigDecimal("9"), true, 5);
        assertThat(transfer.status()).isEqualTo(StockTransferAggregate.DIFFERENCE);
        assertThat(transfer.receivedQty()).isEqualByComparingTo("9");
        assertThat(transfer.differenceQty()).isEqualByComparingTo("1");
        assertThat(transfer.receivedQty().add(transfer.differenceQty())).isEqualByComparingTo(transfer.outboundQty());
    }

    /**
     * 执行命令 {@code rejectsSameWarehouseOverReceiptAndOutOfOrderActions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectsSameWarehouseOverReceiptAndOutOfOrderActions() {
        assertThatThrownBy(() -> StockTransferAggregate.create(1, "TRF-1", 1, 10, 10, "SKU", null, BigDecimal.ONE)).isInstanceOf(BusinessException.class);
        StockTransferAggregate transfer = transfer();
        assertThatThrownBy(() -> transfer.approve(0)).isInstanceOf(BusinessException.class);
        transfer.submit(0);
        transfer.approve(1);
        assertThatThrownBy(() -> transfer.reserve(new BigDecimal("9"), 2)).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsOverReceiptAndRequiresDifferenceResponsibilityEvidence() {
        StockTransferAggregate transfer = transfer();
        transfer.submit(0);
        transfer.approve(1);
        transfer.reserve(new BigDecimal("10"), 2);
        transfer.recordOutbound(new BigDecimal("10"), 3);
        transfer.markInTransit(4);

        assertThatThrownBy(() -> transfer.receive(new BigDecimal("11"), true, 5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不得超过出库量");

        transfer.receive(new BigDecimal("9"), true, 5);
        assertThatThrownBy(() -> transfer.confirmDifference("", "WMS", "EVIDENCE-1", 6))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("差异原因");
        assertThatThrownBy(() -> transfer.confirmDifference("运输短少", "", "EVIDENCE-1", 6))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("责任方");
        assertThatThrownBy(() -> transfer.confirmDifference("运输短少", "CARRIER", "", 6))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("证据");

        transfer.confirmDifference("运输短少", "CARRIER", "EVIDENCE-1", 6);
        assertThat(transfer.differenceReason()).isEqualTo("运输短少");
        assertThat(transfer.responsibleParty()).isEqualTo("CARRIER");
        assertThat(transfer.evidenceRef()).isEqualTo("EVIDENCE-1");
    }

    /**
     * 处理当前类型职责中的操作 {@code transfer}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code StockTransferAggregate}
     */
    private static StockTransferAggregate transfer() {
        return StockTransferAggregate.create(1, "TRF-1", 1, 10, 20, "SKU", null, new BigDecimal("10"));
    }
}
