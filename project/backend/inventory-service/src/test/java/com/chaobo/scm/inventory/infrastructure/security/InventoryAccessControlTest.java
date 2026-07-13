package com.chaobo.scm.inventory.infrastructure.security;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryAccessControlTest {
    @Test
    void ownerAndWarehouseMustBothBeInsideVerifiedDataScope() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated("planner", "n/a", Set.of());
        authentication.setDetails(new ScmAccessContext(7, "planner", "IAM", Set.of("inventory:adjustment:write"),
                Map.of("WAREHOUSE", Set.of("10"), "OWNER", Set.of("20"))));

        InventoryAccessControl.requireAccountScope(authentication, 20, 10);
        assertThatThrownBy(() -> InventoryAccessControl.requireAccountScope(authentication, 21, 10))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> InventoryAccessControl.requireAccountScope(authentication, 20, 11))
                .isInstanceOf(BusinessException.class);
    }
}
