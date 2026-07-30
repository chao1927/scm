package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * IamTokenClaimsProvider。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。以稳定接口声明调用方所需能力，具体实现可在不影响调用方的前提下替换。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@FunctionalInterface
public interface IamTokenClaimsProvider {

    /**
     * 处理当前类型职责中的操作 {@code claimsFor}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PermissionClaims}
     */
    PermissionClaims claimsFor(long userId);

    /**
     * PermissionClaims。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record PermissionClaims(Set<String> permissions, Map<String, Set<String>> dataScopes) {

        public PermissionClaims {
            permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
            dataScopes = dataScopes == null ? Map.of() : Map.copyOf(dataScopes);
        }
    }

    /**
     * DatabaseIamTokenClaimsProvider。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    @Component
    final class DatabaseIamTokenClaimsProvider implements IamTokenClaimsProvider {

        /**
         * mapper（类型：{@code IamPermissionOpenApiMapper}）。
         *
         * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
         */
        private final IamPermissionOpenApiMapper mapper;

        /**
         * 创建 DatabaseIamTokenClaimsProvider。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param mapper 持久化访问依赖，类型为 {@code IamPermissionOpenApiMapper}
         */
        public DatabaseIamTokenClaimsProvider(IamPermissionOpenApiMapper mapper) {
            this.mapper = mapper;
        }

        /**
         * 处理当前类型职责中的操作 {@code claimsFor}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param userId 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code PermissionClaims}
         */
        @Override
        public PermissionClaims claimsFor(long userId) {
            Set<String> permissions = new LinkedHashSet<>();
            mapper.permissionGrants(userId).forEach(grant -> permissions.add(grant.permissionCode()));
            Map<String, Set<String>> dataScopes = new LinkedHashMap<>();
            mapper.dataScopeGrants(userId).forEach(grant -> dataScopes.computeIfAbsent(grant.scopeType(), ignored -> new LinkedHashSet<>()).add(grant.scopeValue()));
            return new PermissionClaims(permissions, dataScopes);
        }
    }
}
