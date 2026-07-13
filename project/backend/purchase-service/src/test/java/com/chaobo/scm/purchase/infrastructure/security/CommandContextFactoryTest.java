package com.chaobo.scm.purchase.infrastructure.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandContextFactoryTest {
    private final CommandContextFactory factory = new CommandContextFactory();

    @Test
    void buildsContextOnlyFromVerifiedPrincipalAndAuthorizedScope() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Operator-Id", "9999");
        request.addHeader("X-Purchase-Org-Id", "1001");
        request.addHeader("X-Idempotency-Key", "idem-1");
        var authentication = new TestingAuthenticationToken("ignored", null);
        authentication.setDetails(new ScmAccessContext(42L, "buyer", "SCM",
                Set.of("purchase:po:create"), Map.of("PURCHASE_ORG", Set.of("1001"))));

        var context = factory.create(request, authentication);

        assertThat(context.operatorId()).isEqualTo(42L);
        assertThat(context.purchaseOrgScope()).isEqualTo(1001L);
        assertThat(context.permissions()).containsExactly("purchase:po:create");
    }

    @Test
    void rejectsMissingAuthenticationAndForgedOrganizationScope() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Purchase-Org-Id", "9999");
        var authentication = new TestingAuthenticationToken("ignored", null);
        authentication.setDetails(new ScmAccessContext(42L, "buyer", "SCM", Set.of("*"),
                Map.of("PURCHASE_ORG", Set.of("1001"))));

        assertThatThrownBy(() -> factory.create(request, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> factory.create(request, authentication)).isInstanceOf(BusinessException.class);
    }
}
