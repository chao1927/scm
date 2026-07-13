package com.chaobo.scm.purchase.infrastructure.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CommandContextFactory {

    public CommandContext create(HttpServletRequest request, Authentication authentication) {
        ScmAccessContext access = accessContext(authentication);
        var tenantId = scopedLong(request, access, "X-Tenant-Id", "TENANT", 0L);
        var purchaseOrgScope = optionalLong(request.getHeader("X-Purchase-Org-Id"));
        if (purchaseOrgScope != null) {
            access.requireScope("PURCHASE_ORG", String.valueOf(purchaseOrgScope));
        }
        return new CommandContext(
                access.operatorId(),
                access.username(),
                tenantId,
                purchaseOrgScope,
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"),
                request.getHeader("X-Idempotency-Key"),
                access.permissions());
    }

    private static ScmAccessContext accessContext(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof ScmAccessContext access)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前请求没有有效访问令牌");
        }
        return access;
    }

    private static long scopedLong(HttpServletRequest request, ScmAccessContext access, String header,
                                   String scopeType, long fallback) {
        String value = request.getHeader(header);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        access.requireScope(scopeType, value);
        return Long.parseLong(value);
    }

    private static Long optionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

}
