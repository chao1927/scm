package com.chaobo.scm.common.error;

/**
 * ErrorCode。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。定义封闭的业务状态或类别集合，避免使用含义不明的数字和字符串。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public enum ErrorCode {

    // 业务枚举值：validation failed
    VALIDATION_FAILED,
    // 业务枚举值：unauthorized
    UNAUTHORIZED,
    // 业务枚举值：forbidden
    FORBIDDEN,
    // 业务枚举值：supplier scope denied
    SUPPLIER_SCOPE_DENIED,
    // 业务枚举值：not found
    NOT_FOUND,
    // 业务枚举值：version conflict
    VERSION_CONFLICT,
    // 业务枚举值：idempotency conflict
    IDEMPOTENCY_CONFLICT,
    // 业务枚举值：state conflict
    STATE_CONFLICT,
    // 业务枚举值：business rule failed
    BUSINESS_RULE_FAILED,
    // 业务枚举值：external call failed
    EXTERNAL_CALL_FAILED,
    // 业务枚举值：system error
    SYSTEM_ERROR
}
