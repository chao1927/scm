package com.chaobo.scm.wms.infrastructure.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.security.core.Authentication;

public final class WmsAccessControl {
    private WmsAccessControl() {
    }

    public static long operatorId(Authentication authentication) {
        return context(authentication).operatorId();
    }

    public static void requireWarehouse(Authentication authentication, long warehouseId) {
        context(authentication).requireScope("WAREHOUSE", String.valueOf(warehouseId));
    }

    private static ScmAccessContext context(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof ScmAccessContext accessContext)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少已验证的访问上下文");
        }
        return accessContext;
    }
}
