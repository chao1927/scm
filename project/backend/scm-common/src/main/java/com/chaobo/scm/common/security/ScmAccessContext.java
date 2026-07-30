package com.chaobo.scm.common.security;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.util.Map;
import java.util.Set;

/**
 * ScmAccessContext。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record ScmAccessContext(long operatorId, String username, String appCode, Set<String> permissions, Map<String, Set<String>> dataScopes) {

    /**
     * 表示不限制功能或数据范围的通配权限。
     */
    private static final String WILDCARD = "*";

    public ScmAccessContext {
        if (operatorId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "访问令牌缺少有效用户标识");
        }
        username = username == null ? "" : username;
        appCode = appCode == null ? "" : appCode;
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        dataScopes = dataScopes == null ? Map.of() : Map.copyOf(dataScopes);
    }

    /**
     * 查询并返回 {@code requirePermission}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param requiredPermission 业务处理参数或成员，类型为 {@code String}
     */
    public void requirePermission(String requiredPermission) {
        if (requiredPermission == null || requiredPermission.isBlank()) {
            throw new IllegalArgumentException("requiredPermission must not be blank");
        }
        boolean allowed = permissions.contains(WILDCARD) || permissions.contains(requiredPermission) || permissions.stream().filter(permission -> permission.endsWith(":*")).map(permission -> permission.substring(0, permission.length() - 1)).anyMatch(requiredPermission::startsWith);
        if (!allowed) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少功能权限: " + requiredPermission);
        }
    }

    /**
     * 查询并返回 {@code requireScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param scopeType 业务处理参数或成员，类型为 {@code String}
     * @param scopeValue 业务处理参数或成员，类型为 {@code String}
     */
    public void requireScope(String scopeType, String scopeValue) {
        if (scopeType == null || scopeType.isBlank() || scopeValue == null || scopeValue.isBlank()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "数据范围不能为空");
        }
        Set<String> values = dataScopes.getOrDefault(scopeType, Set.of());
        if (!values.contains(WILDCARD) && !values.contains(scopeValue)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无数据权限: " + scopeType + "/" + scopeValue);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code allowsScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param scopeType 业务处理参数或成员，类型为 {@code String}
     * @param scopeValue 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean allowsScope(String scopeType, String scopeValue) {
        if (scopeType == null || scopeType.isBlank() || scopeValue == null || scopeValue.isBlank()) {
            return false;
        }
        Set<String> values = dataScopes.getOrDefault(scopeType, Set.of());
        return values.contains(WILDCARD) || values.contains(scopeValue);
    }

    /**
     * 查询并返回 {@code requireApplication}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expectedAppCode 可追踪业务编码，类型为 {@code String}
     */
    public void requireApplication(String expectedAppCode) {
        if (expectedAppCode == null || expectedAppCode.isBlank() || !appCode.equalsIgnoreCase(expectedAppCode.trim())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "应用身份与请求来源不一致: " + expectedAppCode);
        }
    }
}
