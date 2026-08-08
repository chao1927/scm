package com.chaobo.scm.supplier.application.shared;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.util.Set;

/**
 * CommandContext。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record CommandContext(long operatorId, String operatorName, long organizationId, Long supplierScopeId, String requestId, String traceId, String idempotencyKey, Set<String> permissions) {

    public CommandContext {
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    /**
     * 返回写命令必需的幂等键；只读查询可以不携带该请求头。
     *
     * @return 非空幂等键
     */
    public String requiredIdempotencyKey() {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "写请求必须提供 X-Idempotency-Key");
        }
        return idempotencyKey;
    }

    /**
     * 查询并返回 {@code requirePermission}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param permission 业务处理参数或成员，类型为 {@code String}
     */
    public void requirePermission(String permission) {
        String namespaceWildcard = permission.substring(0, permission.indexOf(':') + 1) + "*";
        if (!permissions.contains("*") && !permissions.contains(namespaceWildcard)
                && !permissions.contains(permission)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少权限: " + permission);
        }
    }

    /**
     * 查询并返回 {@code requireSupplierScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     */
    public void requireSupplierScope(long supplierId) {
        if (supplierScopeId != null && supplierScopeId != supplierId) {
            throw new BusinessException(ErrorCode.SUPPLIER_SCOPE_DENIED, "无权操作该供应商数据");
        }
    }
}
