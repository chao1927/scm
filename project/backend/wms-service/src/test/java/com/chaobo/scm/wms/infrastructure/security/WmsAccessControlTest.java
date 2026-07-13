package com.chaobo.scm.wms.infrastructure.security;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WmsAccessControlTest {
    @Test
    void operatorAndWarehouseMustComeFromVerifiedJwtContext() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated("buyer", "n/a", Set.of());
        authentication.setDetails(new ScmAccessContext(42, "buyer", "IAM", Set.of("wms:receipt:write"),
                Map.of("WAREHOUSE", Set.of("100"))));

        assertThat(WmsAccessControl.operatorId(authentication)).isEqualTo(42);
        WmsAccessControl.requireWarehouse(authentication, 100);
        assertThatThrownBy(() -> WmsAccessControl.requireWarehouse(authentication, 999))
                .isInstanceOf(BusinessException.class);
    }
}
