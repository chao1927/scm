package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.infrastructure.jwt.IamJwtService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IamApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class IamApplicationServiceTest {

    /**
     * mapper（类型：{@code MemoryIamMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MemoryIamMapper mapper = new MemoryIamMapper();

    /**
     * service（类型：{@code IamApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamJwtService jwt = new IamJwtService("01234567890123456789012345678901");
    private final TestTokenCache tokenCache = new TestTokenCache();
    private final TestIamSessionMapper sessionMapper = new TestIamSessionMapper();
    private final IamApplicationService service = new IamApplicationService(mapper, jwt,
            userId -> new IamTokenClaimsProvider.PermissionClaims(Set.of(), Map.of()), sessionMapper, tokenCache);

    /**
     * 处理当前类型职责中的操作 {@code userCanLoginRefreshLogoutAndReadMe}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void userCanLoginRefreshLogoutAndReadMe() {
        service.createUser("admin", "123456");
        var login = service.login("admin", "123456");
        var refreshed = service.refresh(login.refreshToken());
        var me = service.me(refreshed.accessToken());
        service.logout(refreshed.refreshToken());
        service.logout(refreshed.refreshToken());
        assertThat(login.accessToken().split("\\.")).hasSize(3);
        assertThat(me.username()).isEqualTo("admin");
        assertThat(sessionMapper.sessions).hasSize(1);
        assertThat(mapper.logs).extracting(IamMapper.OperationLogRow::operation).contains("LOGIN");
    }

    /**
     * 处理当前类型职责中的操作 {@code rolePermissionDataScopeApprovalAndPolicyCanBeManaged}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void rolePermissionDataScopeApprovalAndPolicyCanBeManaged() {
        var user = service.createUser("buyer", "123456");
        var role = service.createRole("BUYER", "采购员");
        service.bindUserRole(user.id(), role.id());
        service.createPermission("PURCHASE", "purchase:po:read", "采购订单读取");
        service.grantRolePermission(role.id(), "purchase:po:read");
        service.createDataScope(role.id(), "WAREHOUSE", "WH-1");
        var approval = service.createApproval("PURCHASE_ORDER", "PO-1");
        service.completeApproval(approval.approvalNo(), true, 0);
        service.createSecurityPolicy("PASSWORD_MIN_LENGTH", "8");
        assertThat(service.dataScopes(role.id())).hasSize(1);
        assertThat(service.permissions(10)).hasSize(1);
        assertThat(service.approvals(10).get(0).status()).isEqualTo(2);
        assertThat(service.securityPolicies(10)).hasSize(1);
    }

    /**
     * 处理当前类型职责中的操作 {@code loginAccessTokenContainsCurrentPermissionsAndDataScopes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void loginAccessTokenContainsCurrentPermissionsAndDataScopes() {
        IamJwtService jwt = new IamJwtService("01234567890123456789012345678901");
        IamApplicationService securedService = new IamApplicationService(mapper, jwt,
                userId -> new IamTokenClaimsProvider.PermissionClaims(Set.of("purchase:po:read"),
                        Map.of("PURCHASE_ORG", Set.of("ORG-1"))), sessionMapper, tokenCache);
        securedService.createUser("buyer", "123456");
        var login = securedService.login("buyer", "123456");
        var accessClaims = jwt.verify(login.accessToken());
        var refreshClaims = jwt.verify(login.refreshToken());
        assertThat(accessClaims.permissions()).containsExactly("purchase:po:read");
        assertThat(accessClaims.dataScopes().get("PURCHASE_ORG")).containsExactly("ORG-1");
        assertThat(refreshClaims.permissions()).isEmpty();
        assertThat(refreshClaims.dataScopes()).isEmpty();
    }

    @Test
    void failsClosedWhenRedisCannotStoreOrValidateSession() {
        service.createUser("closed", "123456");
        tokenCache.unavailable = true;
        assertThatThrownBy(() -> service.login("closed", "123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("redis unavailable");
    }

    @Test
    void rejectsOldRefreshReplayAndRevokesSessionFamily() {
        service.createUser("replay", "123456");
        IamApplicationService.LoginResult first = service.login("replay", "123456");
        service.refresh(first.refreshToken());

        assertThatThrownBy(() -> service.refresh(first.refreshToken()))
                .isInstanceOf(com.chaobo.scm.common.error.BusinessException.class)
                .hasMessageContaining("重放");
        assertThat(tokenCache.sessions.values()).allMatch(session -> !session.active());
    }

    /**
     * MemoryIamMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class MemoryIamMapper implements IamMapper {

        /**
         * users（类型：{@code List<UserRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<UserRow> users = new ArrayList<>();

        /**
         * roles（类型：{@code List<RoleRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<RoleRow> roles = new ArrayList<>();

        /**
         * sessions（类型：{@code List<SessionRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<SessionRow> sessions = new ArrayList<>();

        /**
         * permissions（类型：{@code List<PermissionRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<PermissionRow> permissions = new ArrayList<>();

        /**
         * scopes（类型：{@code List<DataScopeRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<DataScopeRow> scopes = new ArrayList<>();

        /**
         * approvals（类型：{@code List<ApprovalRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<ApprovalRow> approvals = new ArrayList<>();

        /**
         * logs（类型：{@code List<OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<OperationLogRow> logs = new ArrayList<>();

        /**
         * policies（类型：{@code List<SecurityPolicyRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<SecurityPolicyRow> policies = new ArrayList<>();

        /**
         * 查询并返回 {@code findUserByUsername}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param username 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code UserRow}
         */
        public UserRow findUserByUsername(String username) {
            return users.stream().filter(row -> row.username().equals(username)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code findUserById}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 查询并返回的结果，类型为 {@code UserRow}
         */
        public UserRow findUserById(long id) {
            return users.stream().filter(row -> row.id() == id).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code users}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<UserRow>}
         */
        public List<UserRow> users(int limit) {
            return users;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertUser}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param username 业务处理参数或成员，类型为 {@code String}
         * @param passwordHash 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failedAttempts 业务处理参数或成员，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insertUser(long id, String username, String passwordHash, int status, int failedAttempts, int version) {
            users.add(new UserRow(id, username, passwordHash, status, failedAttempts, version));
        }

        /**
         * 执行命令 {@code updateUser}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param passwordHash 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failedAttempts 业务处理参数或成员，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int updateUser(long id, String passwordHash, int status, int failedAttempts, int version, int oldVersion) {
            var row = findUserById(id);
            if (row == null || row.version() != oldVersion) {
                return 0;
            }
            users.set(users.indexOf(row), new UserRow(id, row.username(), passwordHash, status, failedAttempts, version));
            return 1;
        }

        /**
         * 查询并返回 {@code findRole}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param code 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code RoleRow}
         */
        public RoleRow findRole(String code) {
            return roles.stream().filter(row -> row.roleCode().equals(code)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code roles}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RoleRow>}
         */
        public List<RoleRow> roles(int limit) {
            return roles;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertRole}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param name 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insertRole(long id, String code, String name, int status, int version) {
            roles.add(new RoleRow(id, code, name, status, version));
        }

        /**
         * 执行命令 {@code bindUserRole}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param userId 业务或技术标识，类型为 {@code long}
         * @param roleId 业务或技术标识，类型为 {@code long}
         */
        public void bindUserRole(long userId, long roleId) {
        }

        /**
         * 处理当前类型职责中的操作 {@code grantRolePermission}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param roleId 业务或技术标识，类型为 {@code long}
         * @param permissionCode 可追踪业务编码，类型为 {@code String}
         */
        public void grantRolePermission(long roleId, String permissionCode) {
        }

        /**
         * 处理当前类型职责中的操作 {@code insertSession}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param userId 业务或技术标识，类型为 {@code long}
         * @param access 业务处理参数或成员，类型为 {@code String}
         * @param refresh 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insertSession(long id, long userId, String access, String refresh, int status, int version) {
            sessions.add(new SessionRow(id, userId, access, refresh, status, version));
        }

        /**
         * 查询并返回 {@code findSessionByRefresh}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param refreshToken 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SessionRow}
         */
        public SessionRow findSessionByRefresh(String refreshToken) {
            return sessions.stream().filter(row -> row.refreshToken().equals(refreshToken)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code findSessionByAccess}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param accessToken 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SessionRow}
         */
        public SessionRow findSessionByAccess(String accessToken) {
            return sessions.stream().filter(row -> row.accessToken().equals(accessToken)).findFirst().orElse(null);
        }

        /**
         * 执行命令 {@code updateSession}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int updateSession(long id, int status, int version, int oldVersion) {
            var row = sessions.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            sessions.set(sessions.indexOf(row), new SessionRow(row.id(), row.userId(), row.accessToken(), row.refreshToken(), status, version));
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertPermission}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param appCode 可追踪业务编码，类型为 {@code String}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param name 业务处理参数或成员，类型为 {@code String}
         */
        public void insertPermission(long id, String appCode, String code, String name) {
            permissions.add(new PermissionRow(id, appCode, code, name));
        }

        /**
         * 处理当前类型职责中的操作 {@code permissions}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PermissionRow>}
         */
        public List<PermissionRow> permissions(int limit) {
            return permissions;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertDataScope}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param roleId 业务或技术标识，类型为 {@code long}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param value 业务处理参数或成员，类型为 {@code String}
         */
        public void insertDataScope(long id, long roleId, String type, String value) {
            scopes.add(new DataScopeRow(id, roleId, type, value));
        }

        /**
         * 处理当前类型职责中的操作 {@code dataScopes}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param roleId 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DataScopeRow>}
         */
        public List<DataScopeRow> dataScopes(long roleId) {
            return scopes.stream().filter(row -> row.roleId() == roleId).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertApproval}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param businessNo 可追踪业务编码，类型为 {@code String}
         */
        public void insertApproval(long id, String no, String type, String businessNo) {
            approvals.add(new ApprovalRow(id, no, type, businessNo, 1, 0));
        }

        /**
         * 执行命令 {@code completeApproval}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int completeApproval(String no, int status, int version) {
            var row = approvals.stream().filter(v -> v.approvalNo().equals(no) && v.version() == version).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            approvals.set(approvals.indexOf(row), new ApprovalRow(row.id(), row.approvalNo(), row.businessType(), row.businessNo(), status, version + 1));
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code approvals}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ApprovalRow>}
         */
        public List<ApprovalRow> approvals(int limit) {
            return approvals;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param operation 业务处理参数或成员，类型为 {@code String}
         * @param targetNo 可追踪业务编码，类型为 {@code String}
         */
        public void insertOperationLog(long id, String operation, String targetNo) {
            logs.add(new OperationLogRow(id, operation, targetNo));
        }

        /**
         * 处理当前类型职责中的操作 {@code operationLogs}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationLogRow>}
         */
        public List<OperationLogRow> operationLogs(int limit) {
            return logs;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertSecurityPolicy}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param value 业务处理参数或成员，类型为 {@code String}
         */
        public void insertSecurityPolicy(long id, String code, String value) {
            policies.add(new SecurityPolicyRow(id, code, value, 0));
        }

        /**
         * 处理当前类型职责中的操作 {@code securityPolicies}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SecurityPolicyRow>}
         */
        public List<SecurityPolicyRow> securityPolicies(int limit) {
            return policies;
        }
    }
}
