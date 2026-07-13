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

class IamPermissionOpenApiApplicationServiceTest {
    @Test
    void validatesJwtBuildsSnapshotChecksPermissionAndResolvesScope() {
        MemoryIamMapper iamMapper = new MemoryIamMapper();
        MemoryPermissionMapper mapper = new MemoryPermissionMapper();
        IamJwtService jwt = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();
        String accessToken = jwt.issue(new IamJwtService.TokenClaims(
                "1001", "admin", "IAM", "AT-1", "ACCESS", now, now + 3600));
        iamMapper.users.add(new IamMapper.UserRow(1001, "admin", "HASH:ok", 1, 0, 1));
        iamMapper.sessions.add(new IamMapper.SessionRow(1, 1001, accessToken, "refresh", 1, 0));
        mapper.roles.add(new IamPermissionOpenApiMapper.RoleGrantRow("ADMIN", "管理员"));
        mapper.permissions.add(new IamPermissionOpenApiMapper.PermissionGrantRow("purchase:po:read", "PURCHASE", "采购读取"));
        mapper.scopes.add(new IamPermissionOpenApiMapper.DataScopeGrantRow("WAREHOUSE", "WH-1"));
        IamPermissionOpenApiApplicationService service =
                new IamPermissionOpenApiApplicationService(iamMapper, mapper, jwt);

        IamPermissionOpenApiApplicationService.TokenValidationResult token =
                service.validateToken(new IamPermissionOpenApiApplicationService.TokenValidationCommand(accessToken));
        IamPermissionOpenApiApplicationService.PermissionCheckResult check =
                service.checkPermission(new IamPermissionOpenApiApplicationService.PermissionCheckCommand(
                        accessToken, "PURCHASE", "purchase:po:read"));
        IamPermissionOpenApiApplicationService.DataScopeResolveResult scope =
                service.resolveDataScope(new IamPermissionOpenApiApplicationService.DataScopeResolveCommand(
                        accessToken, "PURCHASE", "WAREHOUSE"));

        assertThat(token.valid()).isTrue();
        assertThat(check.allowed()).isTrue();
        assertThat(scope.scopeValues()).containsExactly("WH-1");
        assertThat(service.snapshot(accessToken, "PURCHASE").cacheHit()).isTrue();
    }

    @Test
    void revokedSessionRejectsToken() {
        MemoryIamMapper iamMapper = new MemoryIamMapper();
        MemoryPermissionMapper mapper = new MemoryPermissionMapper();
        IamJwtService jwt = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();
        String accessToken = jwt.issue(new IamJwtService.TokenClaims(
                "1001", "admin", "IAM", "AT-1", "ACCESS", now, now + 3600));
        iamMapper.users.add(new IamMapper.UserRow(1001, "admin", "HASH:ok", 1, 0, 1));
        iamMapper.sessions.add(new IamMapper.SessionRow(1, 1001, accessToken, "refresh", 2, 0));
        IamPermissionOpenApiApplicationService service =
                new IamPermissionOpenApiApplicationService(iamMapper, mapper, jwt);

        assertThatThrownBy(() -> service.validateToken(
                new IamPermissionOpenApiApplicationService.TokenValidationCommand(accessToken)))
                .isInstanceOf(BusinessException.class);
    }

    static class MemoryPermissionMapper implements IamPermissionOpenApiMapper {
        final List<RoleGrantRow> roles = new ArrayList<>();
        final List<PermissionGrantRow> permissions = new ArrayList<>();
        final List<DataScopeGrantRow> scopes = new ArrayList<>();
        final List<PermissionSnapshotRow> snapshots = new ArrayList<>();
        final List<OutboxEventRow> outbox = new ArrayList<>();

        @Override
        public List<RoleGrantRow> roleGrants(long userId) { return roles; }

        @Override
        public List<PermissionGrantRow> permissionGrants(long userId) { return permissions; }

        @Override
        public List<DataScopeGrantRow> dataScopeGrants(long userId) { return scopes; }

        @Override
        public PermissionSnapshotRow findSnapshot(long userId, String appCode) {
            return snapshots.stream()
                    .filter(row -> row.userId() == userId && row.appCode().equals(appCode))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void insertSnapshot(PermissionSnapshotRow row) { snapshots.add(row); }

        @Override
        public void updateSnapshot(PermissionSnapshotRow row) {
            PermissionSnapshotRow existing = findSnapshot(row.userId(), row.appCode());
            snapshots.remove(existing);
            snapshots.add(row);
        }

        @Override
        public void invalidateSnapshots(long userId) {
            snapshots.replaceAll(row -> row.userId() == userId
                    ? new PermissionSnapshotRow(row.userId(), row.appCode(), row.rolePayload(),
                    row.permissionPayload(), row.dataScopePayload(), 2, row.version() + 1, LocalDateTime.now())
                    : row);
        }

        @Override
        public void insertOutbox(OutboxEventRow row) { outbox.add(row); }

        @Override
        public List<OutboxEventRow> listOutbox() { return outbox; }
    }

    static class MemoryIamMapper implements IamMapper {
        final List<UserRow> users = new ArrayList<>();
        final List<SessionRow> sessions = new ArrayList<>();

        @Override
        public UserRow findUserByUsername(String username) { return users.stream().filter(row -> row.username().equals(username)).findFirst().orElse(null); }

        @Override
        public UserRow findUserById(long id) { return users.stream().filter(row -> row.id() == id).findFirst().orElse(null); }

        @Override
        public List<UserRow> users(int limit) { return users; }

        @Override
        public void insertUser(long id, String username, String passwordHash, int status, int failedAttempts, int version) {}

        @Override
        public int updateUser(long id, String passwordHash, int status, int failedAttempts, int version, int oldVersion) { return 0; }

        @Override
        public RoleRow findRole(String code) { return null; }

        @Override
        public List<RoleRow> roles(int limit) { return List.of(); }

        @Override
        public void insertRole(long id, String code, String name, int status, int version) {}

        @Override
        public void bindUserRole(long userId, long roleId) {}

        @Override
        public void grantRolePermission(long roleId, String permissionCode) {}

        @Override
        public void insertSession(long id, long userId, String access, String refresh, int status, int version) {}

        @Override
        public SessionRow findSessionByRefresh(String refreshToken) { return null; }

        @Override
        public SessionRow findSessionByAccess(String accessToken) { return sessions.stream().filter(row -> row.accessToken().equals(accessToken)).findFirst().orElse(null); }

        @Override
        public int updateSession(long id, int status, int version, int oldVersion) { return 0; }

        @Override
        public void insertPermission(long id, String appCode, String code, String name) {}

        @Override
        public List<PermissionRow> permissions(int limit) { return List.of(); }

        @Override
        public void insertDataScope(long id, long roleId, String type, String value) {}

        @Override
        public List<DataScopeRow> dataScopes(long roleId) { return List.of(); }

        @Override
        public void insertApproval(long id, String no, String type, String businessNo) {}

        @Override
        public int completeApproval(String no, int status, int version) { return 0; }

        @Override
        public List<ApprovalRow> approvals(int limit) { return List.of(); }

        @Override
        public void insertOperationLog(long id, String operation, String targetNo) {}

        @Override
        public List<OperationLogRow> operationLogs(int limit) { return List.of(); }

        @Override
        public void insertSecurityPolicy(long id, String code, String value) {}

        @Override
        public List<SecurityPolicyRow> securityPolicies(int limit) { return List.of(); }
    }
}
