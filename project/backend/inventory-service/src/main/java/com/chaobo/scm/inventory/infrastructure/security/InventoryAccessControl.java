package com.chaobo.scm.inventory.infrastructure.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.security.core.Authentication;

public final class InventoryAccessControl {
    private InventoryAccessControl() {
    }

    public static void requireAccountScope(Authentication authentication, long ownerId, long warehouseId) {
        ScmAccessContext context = context(authentication);
        context.requireScope("OWNER", String.valueOf(ownerId));
        context.requireScope("WAREHOUSE", String.valueOf(warehouseId));
    }

    private static ScmAccessContext context(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof ScmAccessContext accessContext)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少已验证的访问上下文");
        }
        return accessContext;
    }
}
