package com.chaobo.scm.supplier.application.score;

import com.chaobo.scm.common.error.*;
import java.math.*;
import java.util.*;

/**
 * SupplierScoringDomainService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierScoringDomainService {

    /**
     * 创建 SupplierScoringDomainService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private SupplierScoringDomainService() {
    }

    /**
     * 处理当前类型职责中的操作 {@code calculate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rules 业务处理参数或成员，类型为 {@code List<Rule>}
     * @param facts 业务处理参数或成员，类型为 {@code List<Fact>}
     * @param adjustment 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    public static Result calculate(List<Rule> rules, List<Fact> facts, BigDecimal adjustment) {
        if (rules == null || rules.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "评分周期内没有已发布规则");
        }
        BigDecimal weight = rules.stream().map(Rule::weight).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weight.compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "评分规则权重之和必须为1");
        }
        Map<String, BigDecimal> dimensions = new LinkedHashMap<>();
        for (var r : rules) {
            var values = facts.stream().filter(f -> f.metricCode().equals(r.metricCode())).map(Fact::value).toList();
            BigDecimal actual = values.isEmpty() ? BigDecimal.ZERO : values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
            BigDecimal ratio = r.direction() == 1 ? actual.divide(r.target(), 6, RoundingMode.HALF_UP) : r.target().divide(actual.signum() == 0 ? BigDecimal.valueOf(0.000001) : actual, 6, RoundingMode.HALF_UP);
            BigDecimal score = ratio.min(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).multiply(r.weight());
            dimensions.merge(r.dimensionCode(), score, BigDecimal::add);
        }
        BigDecimal total = dimensions.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add).add(adjustment == null ? BigDecimal.ZERO : adjustment).max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        return new Result(total, dimensions);
    }

    /**
     * Rule。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Rule(String dimensionCode, String metricCode, BigDecimal weight, BigDecimal target, int direction) {
    }

    /**
     * Fact。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Fact(String metricCode, BigDecimal value) {
    }

    /**
     * Result。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(BigDecimal total, Map<String, BigDecimal> dimensions) {
    }
}
