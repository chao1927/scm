package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WaybillAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WaybillAggregateTest {

    /**
     * 执行命令 {@code createAndVoidWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createAndVoidWaybill() {
        WaybillAggregate waybill = WaybillAggregate.create("WB1", "TMS1", "SF", "顺丰", "SF123", "SF-EXPRESS", "ok");
        waybill.voidWaybill("客户取消", "APR1", 1);
        assertThat(waybill.status()).isEqualTo(WaybillAggregate.VOIDED);
        assertThat(waybill.version()).isEqualTo(2);
        assertThat(waybill.pullEvents()).extracting(TmsEvent::eventType).containsExactly("WaybillCreated", "WaybillVoided");
    }

    /**
     * 执行命令 {@code rejectVoidWithWrongVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectVoidWithWrongVersion() {
        WaybillAggregate waybill = WaybillAggregate.create("WB1", "TMS1", "SF", "顺丰", "SF123", "SF-EXPRESS", "ok");
        assertThatThrownBy(() -> waybill.voidWaybill("客户取消", "APR1", 9)).isInstanceOf(IllegalStateException.class).hasMessageContaining("version conflict");
    }

    @Test
    void carrierFactsAdvanceWithoutAllowingTerminalRegression() {
        WaybillAggregate waybill = WaybillAggregate.create(
            "WB1", "TMS1", "SF", "顺丰", "SF123", "SF-EXPRESS", "ok");
        waybill.advanceFromTrack("ARRIVED");
        waybill.advanceFromTrack("PICKED_UP");
        assertThat(waybill.status()).isEqualTo(WaybillAggregate.ARRIVED);

        waybill.advanceFromReceipt(DeliveryReceiptAggregate.SIGNED);
        waybill.advanceFromTrack("IN_TRANSIT");
        assertThat(waybill.status()).isEqualTo(WaybillAggregate.SIGNED);
        assertThatThrownBy(() ->
            waybill.advanceFromReceipt(DeliveryReceiptAggregate.REJECTED))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("conflicts");
    }
}
