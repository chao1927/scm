package com.chaobo.scm.supplier.domain.item;

import com.chaobo.scm.common.error.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SupplyCondition。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record SupplyCondition(BigDecimal moq, BigDecimal mpq, int leadTimeDays, String purchaseUnit, LocalDate effectiveFrom, LocalDate effectiveTo) {

    public SupplyCondition {
        if (moq == null || moq.signum() <= 0 || mpq == null || mpq.signum() <= 0) {
            throw rule("MOQ和MPQ必须大于0");
        }
        if (leadTimeDays < 0) {
            throw rule("供货周期不能小于0");
        }
        if (purchaseUnit == null || purchaseUnit.isBlank()) {
            throw rule("采购单位不能为空");
        }
        if (effectiveFrom != null && effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw rule("供货失效日不能早于生效日");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String message) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, message);
    }
}
