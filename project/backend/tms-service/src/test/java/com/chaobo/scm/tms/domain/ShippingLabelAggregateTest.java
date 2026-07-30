package com.chaobo.scm.tms.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ShippingLabelAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ShippingLabelAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code generateAndPrintLabel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void generateAndPrintLabel() {
        ShippingLabelAggregate label = ShippingLabelAggregate.generate("LBL1", "WB1", "PKG1", "SF-V1", "oss://labels/LBL1.pdf");
        label.print("PRINTER-1");
        assertThat(label.status()).isEqualTo(ShippingLabelAggregate.PRINTED);
        assertThat(label.printCount()).isEqualTo(1);
        assertThat(label.pullEvents()).extracting(TmsEvent::eventType).containsExactly("ShippingLabelGenerated", "ShippingLabelPrinted");
    }

    /**
     * 执行命令 {@code rejectPrintVoidedLabel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectPrintVoidedLabel() {
        ShippingLabelAggregate label = ShippingLabelAggregate.generate("LBL1", "WB1", "PKG1", "SF-V1", "oss://labels/LBL1.pdf");
        label.voidLabel("运单作废");
        assertThatThrownBy(() -> label.print("PRINTER-1")).isInstanceOf(IllegalStateException.class).hasMessageContaining("voided label");
    }
}
