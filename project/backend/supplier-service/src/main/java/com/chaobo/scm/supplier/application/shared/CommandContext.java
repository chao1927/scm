package com.chaobo.scm.supplier.application.shared;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.util.Set;

public record CommandContext(long operatorId, String operatorName, long organizationId,
                             Long supplierScopeId, String requestId, String traceId,
                             String idempotencyKey, Set<String> permissions) {
    public CommandContext {
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "写请求必须提供 X-Idempotency-Key");
        }
    }

    public void requirePermission(String permission) {
        if (!permissions.contains(permission)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少权限: " + permission);
        }
    }

    public void requireSupplierScope(long supplierId) {
        if (supplierScopeId != null && supplierScopeId != supplierId) {
            throw new BusinessException(ErrorCode.SUPPLIER_SCOPE_DENIED, "无权操作该供应商数据");
        }
    }
}
