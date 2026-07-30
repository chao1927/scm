package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LogisticsSettlementAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class LogisticsSettlementAggregateTest {

    /**
     * 执行命令 {@code closeExceptionRequiresVersionAndResponsibleParty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void closeExceptionRequiresVersionAndResponsibleParty() {
        LogisticsExceptionAggregate exception = LogisticsExceptionAggregate.register("EXC1", "WB1", "DAMAGED", "P1", "外包装破损", "CARRIER");
        exception.close("已索赔", "CARRIER", 1);
        assertThat(exception.status()).isEqualTo(LogisticsExceptionAggregate.CLOSED);
        assertThat(exception.pullEvents()).extracting(TmsEvent::eventType).containsExactly("LogisticsExceptionRegistered", "LogisticsExceptionClosed");
    }

    /**
     * 执行命令 {@code rejectCloseExceptionWithoutResponsibleParty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectCloseExceptionWithoutResponsibleParty() {
        LogisticsExceptionAggregate exception = LogisticsExceptionAggregate.register("EXC1", "WB1", "DAMAGED", "P1", "外包装破损", null);
        assertThatThrownBy(() -> exception.close("已索赔", "", 1)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("responsible party");
    }

    /**
     * 处理当前类型职责中的操作 {@code generateAndPushFeeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void generateAndPushFeeSource() {
        LogisticsFeeSourceAggregate feeSource = LogisticsFeeSourceAggregate.generate("FEE1", "WB1", "SF", "SF-EXPRESS", "FREIGHT", new BigDecimal("12.30"), "CNY", "202607", "SHIPPER");
        feeSource.pushToBms("BMS1");
        assertThat(feeSource.pushStatus()).isEqualTo(LogisticsFeeSourceAggregate.PUSHED);
        assertThat(feeSource.pullEvents()).extracting(TmsEvent::eventType).containsExactly("LogisticsFeeSourceGenerated", "LogisticsFeeSourcePushed");
    }
}
