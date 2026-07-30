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

/**
 * CommandContextFactoryTest。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class CommandContextFactoryTest {

    /**
     * factory（类型：{@code CommandContextFactory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final CommandContextFactory factory = new CommandContextFactory();

    /**
     * 处理当前类型职责中的操作 {@code buildsContextOnlyFromVerifiedPrincipalAndAuthorizedScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void buildsContextOnlyFromVerifiedPrincipalAndAuthorizedScope() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Operator-Id", "9999");
        request.addHeader("X-Purchase-Org-Id", "1001");
        request.addHeader("X-Idempotency-Key", "idem-1");
        var authentication = new TestingAuthenticationToken("ignored", null);
        authentication.setDetails(new ScmAccessContext(42L, "buyer", "SCM", Set.of("purchase:po:create"), Map.of("PURCHASE_ORG", Set.of("1001"))));
        var context = factory.create(request, authentication);
        assertThat(context.operatorId()).isEqualTo(42L);
        assertThat(context.purchaseOrgScope()).isEqualTo(1001L);
        assertThat(context.permissions()).containsExactly("purchase:po:create");
    }

    /**
     * 执行命令 {@code rejectsMissingAuthenticationAndForgedOrganizationScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectsMissingAuthenticationAndForgedOrganizationScope() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Purchase-Org-Id", "9999");
        var authentication = new TestingAuthenticationToken("ignored", null);
        authentication.setDetails(new ScmAccessContext(42L, "buyer", "SCM", Set.of("*"), Map.of("PURCHASE_ORG", Set.of("1001"))));
        assertThatThrownBy(() -> factory.create(request, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> factory.create(request, authentication)).isInstanceOf(BusinessException.class);
    }
}
