package com.chaobo.scm.inventory.infrastructure.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.security.core.Authentication;

/**
 * InventoryAccessControl。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class InventoryAccessControl {

    /**
     * 创建 InventoryAccessControl。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private InventoryAccessControl() {
    }

    /**
     * 查询并返回 {@code requireAccountScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     */
    public static void requireAccountScope(Authentication authentication, long ownerId, long warehouseId) {
        ScmAccessContext context = context(authentication);
        context.requireScope("OWNER", String.valueOf(ownerId));
        context.requireScope("WAREHOUSE", String.valueOf(warehouseId));
    }

    /**
     * 处理当前类型职责中的操作 {@code context}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ScmAccessContext}
     */
    private static ScmAccessContext context(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof ScmAccessContext accessContext)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少已验证的访问上下文");
        }
        return accessContext;
    }
}
