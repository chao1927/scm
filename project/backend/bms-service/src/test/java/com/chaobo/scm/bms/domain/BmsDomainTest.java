package com.chaobo.scm.bms.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BmsDomainTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class BmsDomainTest {

    /**
     * 执行命令 {@code publishedRuleCalculatesTaxIncludedAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void publishedRuleCalculatesTaxIncludedAmount() {
        BmsDomain.BillingRuleAggregate rule = BmsDomain.BillingRuleAggregate.create("BR1", "BO1", "FREIGHT", new BigDecimal("10.0000"), new BigDecimal("0.1000"), LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"));
        rule.publish(1);
        BmsDomain.ChargeAmount amount = rule.calculate(new BigDecimal("2.5"));
        assertThat(amount.amount()).isEqualByComparingTo("25.00");
        assertThat(amount.taxAmount()).isEqualByComparingTo("2.50");
        assertThat(amount.totalAmount()).isEqualByComparingTo("27.50");
        assertThat(rule.effectiveOn(LocalDate.parse("2026-07-01"))).isTrue();
    }

    /**
     * 执行命令 {@code confirmedChargeCannotRecalculate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void confirmedChargeCannotRecalculate() {
        BmsDomain.ChargeDetailAggregate charge = BmsDomain.ChargeDetailAggregate.create("CD1", "CS1", "BO1", "FREIGHT", "BR1", BigDecimal.ONE, BigDecimal.TEN, new BmsDomain.ChargeAmount(BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("11.00")));
        charge.confirm();
        assertThatThrownBy(() -> charge.recalculate(BigDecimal.TEN, BigDecimal.ONE, new BmsDomain.ChargeAmount(BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN), charge.version())).isInstanceOf(IllegalStateException.class).hasMessageContaining("adjustment");
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciliationAndInvoiceProtectAmountInvariants}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void reconciliationAndInvoiceProtectAmountInvariants() {
        BmsDomain.ReconciliationAggregate reconciliation = BmsDomain.ReconciliationAggregate.create("RC1", "BO1", "2026-07", new BigDecimal("100.00"));
        assertThatThrownBy(() -> reconciliation.confirm(new BigDecimal("99.99"), reconciliation.version())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BmsDomain.InvoiceAggregate.request("IV1", "BL1", new BigDecimal("120.00"), new BigDecimal("100.00"))).isInstanceOf(IllegalArgumentException.class);
    }
}
