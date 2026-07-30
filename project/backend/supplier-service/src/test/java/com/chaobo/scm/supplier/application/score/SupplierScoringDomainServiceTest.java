package com.chaobo.scm.supplier.application.score;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

/**
 * SupplierScoringDomainServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class SupplierScoringDomainServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code shouldCalculateExplainableWeightedScore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldCalculateExplainableWeightedScore() {
        var rules = List.of(new SupplierScoringDomainService.Rule("QUALITY", "PASS_RATE", new BigDecimal("0.6"), new BigDecimal("0.98"), 1), new SupplierScoringDomainService.Rule("DELIVERY", "DELAY_RATE", new BigDecimal("0.4"), new BigDecimal("0.02"), 2));
        var facts = List.of(new SupplierScoringDomainService.Fact("PASS_RATE", new BigDecimal("0.931")), new SupplierScoringDomainService.Fact("DELAY_RATE", new BigDecimal("0.04")));
        var result = SupplierScoringDomainService.calculate(rules, facts, BigDecimal.ZERO);
        assertThat(result.total()).isEqualByComparingTo("77.00");
        assertThat(result.dimensions()).containsKeys("QUALITY", "DELIVERY");
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldRejectInvalidWeightSum}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRejectInvalidWeightSum() {
        var rules = List.of(new SupplierScoringDomainService.Rule("Q", "M", new BigDecimal("0.8"), BigDecimal.ONE, 1));
        assertThatThrownBy(() -> SupplierScoringDomainService.calculate(rules, List.of(), BigDecimal.ZERO)).hasMessageContaining("权重");
    }
}
