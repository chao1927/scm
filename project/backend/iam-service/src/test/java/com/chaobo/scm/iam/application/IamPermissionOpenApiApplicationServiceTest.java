package com.chaobo.scm.iam.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.iam.infrastructure.jwt.IamJwtService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IamPermissionOpenApiApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class IamPermissionOpenApiApplicationServiceTest {

    /**
     * 校验业务约束 {@code validatesJwtBuildsSnapshotChecksPermissionAndResolvesScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void validatesJwtBuildsSnapshotChecksPermissionAndResolvesScope() {
        MemoryIamMapper iamMapper = new MemoryIamMapper();
        MemoryPermissionMapper mapper = new MemoryPermissionMapper();
        IamJwtService jwt = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();
        String accessToken = jwt.issue(new IamJwtService.TokenClaims("1001", "admin", "IAM", "AT-1", "ACCESS", now, now + 3600));
        iamMapper.users.add(new IamMapper.UserRow(1001, "admin", "HASH:ok", 1, 0, 1));
        iamMapper.sessions.add(new IamMapper.SessionRow(1, 1001, accessToken, "refresh", 1, 0));
        mapper.roles.add(new IamPermissionOpenApiMapper.RoleGrantRow("ADMIN", "管理员"));
        mapper.permissions.add(new IamPermissionOpenApiMapper.PermissionGrantRow("purchase:po:read", "PURCHASE", "采购读取"));
        mapper.scopes.add(new IamPermissionOpenApiMapper.DataScopeGrantRow("WAREHOUSE", "WH-1"));
        TestTokenCache tokenCache = new TestTokenCache();
        tokenCache.store(new TokenCachePort.OnlineSession(1, 1001, "AT-1", "RT-1", 0,
                now + 3600, now + 86400, true));
        IamPermissionOpenApiApplicationService service = new IamPermissionOpenApiApplicationService(iamMapper, mapper, jwt, tokenCache);
        IamPermissionOpenApiApplicationService.TokenValidationResult token = service.validateToken(new IamPermissionOpenApiApplicationService.TokenValidationCommand(accessToken));
        IamPermissionOpenApiApplicationService.PermissionCheckResult check = service.checkPermission(new IamPermissionOpenApiApplicationService.PermissionCheckCommand(accessToken, "PURCHASE", "purchase:po:read"));
        IamPermissionOpenApiApplicationService.DataScopeResolveResult scope = service.resolveDataScope(new IamPermissionOpenApiApplicationService.DataScopeResolveCommand(accessToken, "PURCHASE", "WAREHOUSE"));
        assertThat(token.valid()).isTrue();
        assertThat(check.allowed()).isTrue();
        assertThat(scope.scopeValues()).containsExactly("WH-1");
        assertThat(service.snapshot(accessToken, "PURCHASE").cacheHit()).isTrue();
    }

    /**
     * 处理当前类型职责中的操作 {@code revokedSessionRejectsToken}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void revokedSessionRejectsToken() {
        MemoryIamMapper iamMapper = new MemoryIamMapper();
        MemoryPermissionMapper mapper = new MemoryPermissionMapper();
        IamJwtService jwt = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();
        String accessToken = jwt.issue(new IamJwtService.TokenClaims("1001", "admin", "IAM", "AT-1", "ACCESS", now, now + 3600));
        iamMapper.users.add(new IamMapper.UserRow(1001, "admin", "HASH:ok", 1, 0, 1));
        iamMapper.sessions.add(new IamMapper.SessionRow(1, 1001, accessToken, "refresh", 2, 0));
        TestTokenCache tokenCache = new TestTokenCache();
        tokenCache.store(new TokenCachePort.OnlineSession(1, 1001, "AT-2", "RT-2", 0,
                now + 3600, now + 86400, false));
        IamPermissionOpenApiApplicationService service = new IamPermissionOpenApiApplicationService(iamMapper, mapper, jwt, tokenCache);
        assertThatThrownBy(() -> service.validateToken(new IamPermissionOpenApiApplicationService.TokenValidationCommand(accessToken))).isInstanceOf(BusinessException.class);
    }

    /**
     * MemoryPermissionMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryPermissionMapper implements IamPermissionOpenApiMapper {

        /**
         * roles（类型：{@code List<RoleGrantRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<RoleGrantRow> roles = new ArrayList<>();

        /**
         * permissions（类型：{@code List<PermissionGrantRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<PermissionGrantRow> permissions = new ArrayList<>();

        /**
         * scopes（类型：{@code List<DataScopeGrantRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<DataScopeGrantRow> scopes = new ArrayList<>();

        /**
         * snapshots（类型：{@code List<PermissionSnapshotRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<PermissionSnapshotRow> snapshots = new ArrayList<>();

        /**
         * outbox（类型：{@code List<OutboxEventRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OutboxEventRow> outbox = new ArrayList<>();

        /**
         * 处理当前类型职责中的操作 {@code roleGrants}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param userId 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RoleGrantRow>}
         */
        @Override
        public List<RoleGrantRow> roleGrants(long userId) {
            return roles;
        }

        /**
         * 处理当前类型职责中的操作 {@code permissionGrants}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param userId 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PermissionGrantRow>}
         */
        @Override
        public List<PermissionGrantRow> permissionGrants(long userId) {
            return permissions;
        }

        /**
         * 处理当前类型职责中的操作 {@code dataScopeGrants}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param userId 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DataScopeGrantRow>}
         */
        @Override
        public List<DataScopeGrantRow> dataScopeGrants(long userId) {
            return scopes;
        }

        /**
         * 查询并返回 {@code findSnapshot}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param userId 业务或技术标识，类型为 {@code long}
         * @param appCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code PermissionSnapshotRow}
         */
        @Override
        public PermissionSnapshotRow findSnapshot(long userId, String appCode) {
            return snapshots.stream().filter(row -> row.userId() == userId && row.appCode().equals(appCode)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertSnapshot}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code PermissionSnapshotRow}
         */
        @Override
        public void insertSnapshot(PermissionSnapshotRow row) {
            snapshots.add(row);
        }

        /**
         * 执行命令 {@code updateSnapshot}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code PermissionSnapshotRow}
         */
        @Override
        public void updateSnapshot(PermissionSnapshotRow row) {
            PermissionSnapshotRow existing = findSnapshot(row.userId(), row.appCode());
            snapshots.remove(existing);
            snapshots.add(row);
        }

        /**
         * 处理当前类型职责中的操作 {@code invalidateSnapshots}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param userId 业务或技术标识，类型为 {@code long}
         */
        @Override
        public void invalidateSnapshots(long userId) {
            snapshots.replaceAll(row -> row.userId() == userId ? new PermissionSnapshotRow(row.userId(), row.appCode(), row.rolePayload(), row.permissionPayload(), row.dataScopePayload(), 2, row.version() + 1, LocalDateTime.now()) : row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboxEventRow}
         */
        @Override
        public void insertOutbox(OutboxEventRow row) {
            outbox.add(row);
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboxEventRow>}
         */
        @Override
        public List<OutboxEventRow> listOutbox() {
            return outbox;
        }
    }

    /**
     * MemoryIamMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryIamMapper implements IamMapper {

        /**
         * users（类型：{@code List<UserRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<UserRow> users = new ArrayList<>();

        /**
         * sessions（类型：{@code List<SessionRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<SessionRow> sessions = new ArrayList<>();

        /**
         * 查询并返回 {@code findUserByUsername}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param username 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code UserRow}
         */
        @Override
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
        @Override
        public UserRow findUserById(long id) {
            return users.stream().filter(row -> row.id() == id).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code users}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<UserRow>}
         */
        @Override
        public List<UserRow> users(int limit) {
            return users;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertUser}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param username 业务处理参数或成员，类型为 {@code String}
         * @param passwordHash 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failedAttempts 业务处理参数或成员，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        @Override
        public void insertUser(long id, String username, String passwordHash, int status, int failedAttempts, int version) {
        }

        /**
         * 执行命令 {@code updateUser}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param passwordHash 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param failedAttempts 业务处理参数或成员，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int updateUser(long id, String passwordHash, int status, int failedAttempts, int version, int oldVersion) {
            return 0;
        }

        /**
         * 查询并返回 {@code findRole}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param code 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code RoleRow}
         */
        @Override
        public RoleRow findRole(String code) {
            return null;
        }

        /**
         * 处理当前类型职责中的操作 {@code roles}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RoleRow>}
         */
        @Override
        public List<RoleRow> roles(int limit) {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertRole}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param name 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        @Override
        public void insertRole(long id, String code, String name, int status, int version) {
        }

        /**
         * 执行命令 {@code bindUserRole}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param userId 业务或技术标识，类型为 {@code long}
         * @param roleId 业务或技术标识，类型为 {@code long}
         */
        @Override
        public void bindUserRole(long userId, long roleId) {
        }

        /**
         * 处理当前类型职责中的操作 {@code grantRolePermission}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param roleId 业务或技术标识，类型为 {@code long}
         * @param permissionCode 可追踪业务编码，类型为 {@code String}
         */
        @Override
        public void grantRolePermission(long roleId, String permissionCode) {
        }

        /**
         * 处理当前类型职责中的操作 {@code insertSession}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param userId 业务或技术标识，类型为 {@code long}
         * @param access 业务处理参数或成员，类型为 {@code String}
         * @param refresh 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        @Override
        public void insertSession(long id, long userId, String access, String refresh, int status, int version) {
        }

        /**
         * 查询并返回 {@code findSessionByRefresh}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param refreshToken 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SessionRow}
         */
        @Override
        public SessionRow findSessionByRefresh(String refreshToken) {
            return null;
        }

        /**
         * 查询并返回 {@code findSessionByAccess}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param accessToken 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code SessionRow}
         */
        @Override
        public SessionRow findSessionByAccess(String accessToken) {
            return sessions.stream().filter(row -> row.accessToken().equals(accessToken)).findFirst().orElse(null);
        }

        /**
         * 执行命令 {@code updateSession}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int updateSession(long id, int status, int version, int oldVersion) {
            return 0;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertPermission}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param appCode 可追踪业务编码，类型为 {@code String}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param name 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void insertPermission(long id, String appCode, String code, String name) {
        }

        /**
         * 处理当前类型职责中的操作 {@code permissions}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PermissionRow>}
         */
        @Override
        public List<PermissionRow> permissions(int limit) {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertDataScope}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param roleId 业务或技术标识，类型为 {@code long}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param value 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void insertDataScope(long id, long roleId, String type, String value) {
        }

        /**
         * 处理当前类型职责中的操作 {@code dataScopes}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param roleId 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DataScopeRow>}
         */
        @Override
        public List<DataScopeRow> dataScopes(long roleId) {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertApproval}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param businessNo 可追踪业务编码，类型为 {@code String}
         */
        @Override
        public void insertApproval(long id, String no, String type, String businessNo) {
        }

        /**
         * 执行命令 {@code completeApproval}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int completeApproval(String no, int status, int version) {
            return 0;
        }

        /**
         * 处理当前类型职责中的操作 {@code approvals}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ApprovalRow>}
         */
        @Override
        public List<ApprovalRow> approvals(int limit) {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param operation 业务处理参数或成员，类型为 {@code String}
         * @param targetNo 可追踪业务编码，类型为 {@code String}
         */
        @Override
        public void insertOperationLog(long id, String operation, String targetNo) {
        }

        /**
         * 处理当前类型职责中的操作 {@code operationLogs}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationLogRow>}
         */
        @Override
        public List<OperationLogRow> operationLogs(int limit) {
            return List.of();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertSecurityPolicy}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param value 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void insertSecurityPolicy(long id, String code, String value) {
        }

        /**
         * 处理当前类型职责中的操作 {@code securityPolicies}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SecurityPolicyRow>}
         */
        @Override
        public List<SecurityPolicyRow> securityPolicies(int limit) {
            return List.of();
        }
    }
}
