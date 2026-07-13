package com.chaobo.scm.purchase.application.shared;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.util.Set;

public record CommandContext(
        long operatorId,
        String operatorName,
        long tenantId,
        Long purchaseOrgScope,
        String requestId,
        String traceId,
        String idempotencyKey,
        Set<String> permissions) {

    public void requirePermission(String permission) {
        boolean allowed = permissions != null && (permissions.contains("*") || permissions.contains(permission)
                || permissions.stream().filter(value -> value.endsWith(":*"))
                .map(value -> value.substring(0, value.length() - 1)).anyMatch(permission::startsWith));
        if (!allowed) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少功能权限: " + permission);
        }
    }

    public void requirePurchaseOrgScope(long purchaseOrgId) {
        if (purchaseOrgScope != null && purchaseOrgScope != purchaseOrgId) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无采购组织数据权限");
        }
    }

    public String requiredIdempotencyKey() {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "写接口必须传入X-Idempotency-Key");
        }
        return idempotencyKey;
    }
}
