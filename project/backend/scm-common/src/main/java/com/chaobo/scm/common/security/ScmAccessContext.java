package com.chaobo.scm.common.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.util.Map;
import java.util.Set;

public record ScmAccessContext(
        long operatorId,
        String username,
        String appCode,
        Set<String> permissions,
        Map<String, Set<String>> dataScopes
) {
    public ScmAccessContext {
        if (operatorId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "访问令牌缺少有效用户标识");
        }
        username = username == null ? "" : username;
        appCode = appCode == null ? "" : appCode;
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        dataScopes = dataScopes == null ? Map.of() : Map.copyOf(dataScopes);
    }

    public void requirePermission(String requiredPermission) {
        if (requiredPermission == null || requiredPermission.isBlank()) {
            throw new IllegalArgumentException("requiredPermission must not be blank");
        }
        boolean allowed = permissions.contains("*") || permissions.contains(requiredPermission)
                || permissions.stream().filter(permission -> permission.endsWith(":*"))
                .map(permission -> permission.substring(0, permission.length() - 1))
                .anyMatch(requiredPermission::startsWith);
        if (!allowed) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少功能权限: " + requiredPermission);
        }
    }

    public void requireScope(String scopeType, String scopeValue) {
        if (scopeType == null || scopeType.isBlank() || scopeValue == null || scopeValue.isBlank()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "数据范围不能为空");
        }
        Set<String> values = dataScopes.getOrDefault(scopeType, Set.of());
        if (!values.contains("*") && !values.contains(scopeValue)) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                    "无数据权限: " + scopeType + "/" + scopeValue);
        }
    }
}
