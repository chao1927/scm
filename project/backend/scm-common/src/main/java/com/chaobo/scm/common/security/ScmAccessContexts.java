package com.chaobo.scm.common.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.security.core.Authentication;

/**
 * ScmAccessContexts。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class ScmAccessContexts {

    /**
     * 创建 ScmAccessContexts。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private ScmAccessContexts() {
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param authentication 业务处理参数或成员，类型为 {@code Authentication}
     * @return 查询并返回的结果，类型为 {@code ScmAccessContext}
     */
    public static ScmAccessContext require(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof ScmAccessContext context)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少已验证的访问上下文");
        }
        return context;
    }
}
