package com.chaobo.scm.inventory.infrastructure.security;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InventoryAccessControlTest。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class InventoryAccessControlTest {

    /**
     * 处理当前类型职责中的操作 {@code ownerAndWarehouseMustBothBeInsideVerifiedDataScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void ownerAndWarehouseMustBothBeInsideVerifiedDataScope() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated("planner", "n/a", Set.of());
        authentication.setDetails(new ScmAccessContext(7, "planner", "IAM", Set.of("inventory:adjustment:write"), Map.of("WAREHOUSE", Set.of("10"), "OWNER", Set.of("20"))));
        InventoryAccessControl.requireAccountScope(authentication, 20, 10);
        assertThatThrownBy(() -> InventoryAccessControl.requireAccountScope(authentication, 21, 10)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> InventoryAccessControl.requireAccountScope(authentication, 20, 11)).isInstanceOf(BusinessException.class);
    }
}
