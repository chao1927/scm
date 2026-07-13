package com.chaobo.scm.common.security;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScmAccessContextTest {
    @Test
    void acceptsExactAndNamespacePermissions() {
        var context = new ScmAccessContext(7L, "buyer", "SCM",
                Set.of("purchase:po:read", "wms:*"), Map.of());

        context.requirePermission("purchase:po:read");
        context.requirePermission("wms:receipt:write");

        assertThatThrownBy(() -> context.requirePermission("purchase:po:approve"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsDataOutsideTokenScope() {
        var context = new ScmAccessContext(7L, "buyer", "SCM", Set.of("*"),
                Map.of("ORGANIZATION", Set.of("1001"), "WAREHOUSE", Set.of("WH01", "WH02")));

        context.requireScope("WAREHOUSE", "WH02");

        assertThatThrownBy(() -> context.requireScope("WAREHOUSE", "WH99"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> context.requireScope("OWNER", "2001"))
                .isInstanceOf(BusinessException.class);
    }
}
