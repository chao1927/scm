package com.chaobo.scm.supplier.domain.profile;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.util.Set;

/**
 * ProfileFieldChange。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record ProfileFieldChange(String fieldCode, String beforeValue, String afterValue) {

    /**
     * IMMUTABLE_FIELDS（类型：{@code Set<String>}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Set<String> IMMUTABLE_FIELDS = Set.of("supplierId", "supplierCode", "lifecycleStatus");

    public ProfileFieldChange {
        if (fieldCode == null || fieldCode.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "变更字段编码不能为空");
        }
        if (IMMUTABLE_FIELDS.contains(fieldCode)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, fieldCode + " 不允许通过资料变更修改");
        }
        if (java.util.Objects.equals(beforeValue, afterValue)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, fieldCode + " 的新旧值不能相同");
        }
    }
}
