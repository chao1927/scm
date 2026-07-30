package com.chaobo.scm.common.security;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ScmAccessContextTest。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ScmAccessContextTest {

    /**
     * 处理当前类型职责中的操作 {@code acceptsExactAndNamespacePermissions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void acceptsExactAndNamespacePermissions() {
        var context = new ScmAccessContext(7L, "buyer", "SCM", Set.of("purchase:po:read", "wms:*"), Map.of());
        context.requirePermission("purchase:po:read");
        context.requirePermission("wms:receipt:write");
        assertThatThrownBy(() -> context.requirePermission("purchase:po:approve")).isInstanceOf(BusinessException.class);
    }

    /**
     * 执行命令 {@code rejectsDataOutsideTokenScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rejectsDataOutsideTokenScope() {
        var context = new ScmAccessContext(7L, "buyer", "SCM", Set.of("*"), Map.of("ORGANIZATION", Set.of("1001"), "WAREHOUSE", Set.of("WH01", "WH02")));
        context.requireScope("WAREHOUSE", "WH02");
        org.assertj.core.api.Assertions.assertThat(context.allowsScope("WAREHOUSE", "WH02")).isTrue();
        org.assertj.core.api.Assertions.assertThat(context.allowsScope("WAREHOUSE", "WH99")).isFalse();
        assertThatThrownBy(() -> context.requireScope("WAREHOUSE", "WH99")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> context.requireScope("OWNER", "2001")).isInstanceOf(BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code applicationIdentityCannotImpersonateAnotherSourceSystem}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void applicationIdentityCannotImpersonateAnotherSourceSystem() {
        var context = new ScmAccessContext(9L, "purchase-service", "PURCHASE", Set.of("integration:event:write"), Map.of());
        context.requireApplication("PURCHASE");
        assertThatThrownBy(() -> context.requireApplication("OMS")).isInstanceOf(BusinessException.class);
    }
}
