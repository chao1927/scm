package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.infrastructure.jwt.IamJwtService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IamApplicationServiceTest {
    private final MemoryIamMapper mapper = new MemoryIamMapper();
    private final IamApplicationService service = new IamApplicationService(mapper);

    @Test
    void userCanLoginRefreshLogoutAndReadMe() {
        service.createUser("admin", "123456");

        var login = service.login("admin", "123456");
        var refreshed = service.refresh(login.refreshToken());
        var me = service.me(login.accessToken());
        service.logout(refreshed.refreshToken());

        assertThat(login.accessToken().split("\\.")).hasSize(3);
        assertThat(me.username()).isEqualTo("admin");
        assertThat(mapper.sessions).hasSize(2);
        assertThat(mapper.logs).extracting(IamMapper.OperationLogRow::operation).contains("LOGIN");
    }

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

    @Test
    void loginAccessTokenContainsCurrentPermissionsAndDataScopes() {
        IamJwtService jwt = new IamJwtService("01234567890123456789012345678901");
        IamApplicationService securedService = new IamApplicationService(mapper, jwt, userId ->
                new IamTokenClaimsProvider.PermissionClaims(
                        Set.of("purchase:po:read"), Map.of("PURCHASE_ORG", Set.of("ORG-1"))));
        securedService.createUser("buyer", "123456");

        var login = securedService.login("buyer", "123456");
        var accessClaims = jwt.verify(login.accessToken());
        var refreshClaims = jwt.verify(login.refreshToken());

        assertThat(accessClaims.permissions()).containsExactly("purchase:po:read");
        assertThat(accessClaims.dataScopes().get("PURCHASE_ORG")).containsExactly("ORG-1");
        assertThat(refreshClaims.permissions()).isEmpty();
        assertThat(refreshClaims.dataScopes()).isEmpty();
    }

    private static class MemoryIamMapper implements IamMapper {
        private final List<UserRow> users = new ArrayList<>();
        private final List<RoleRow> roles = new ArrayList<>();
        private final List<SessionRow> sessions = new ArrayList<>();
        private final List<PermissionRow> permissions = new ArrayList<>();
        private final List<DataScopeRow> scopes = new ArrayList<>();
        private final List<ApprovalRow> approvals = new ArrayList<>();
        private final List<OperationLogRow> logs = new ArrayList<>();
        private final List<SecurityPolicyRow> policies = new ArrayList<>();

        public UserRow findUserByUsername(String username) { return users.stream().filter(row -> row.username().equals(username)).findFirst().orElse(null); }
        public UserRow findUserById(long id) { return users.stream().filter(row -> row.id() == id).findFirst().orElse(null); }
        public List<UserRow> users(int limit) { return users; }
        public void insertUser(long id, String username, String passwordHash, int status, int failedAttempts, int version) { users.add(new UserRow(id, username, passwordHash, status, failedAttempts, version)); }
        public int updateUser(long id, String passwordHash, int status, int failedAttempts, int version, int oldVersion) { var row = findUserById(id); if (row == null || row.version() != oldVersion) return 0; users.set(users.indexOf(row), new UserRow(id, row.username(), passwordHash, status, failedAttempts, version)); return 1; }
        public RoleRow findRole(String code) { return roles.stream().filter(row -> row.roleCode().equals(code)).findFirst().orElse(null); }
        public List<RoleRow> roles(int limit) { return roles; }
        public void insertRole(long id, String code, String name, int status, int version) { roles.add(new RoleRow(id, code, name, status, version)); }
        public void bindUserRole(long userId, long roleId) {}
        public void grantRolePermission(long roleId, String permissionCode) {}
        public void insertSession(long id, long userId, String access, String refresh, int status, int version) { sessions.add(new SessionRow(id, userId, access, refresh, status, version)); }
        public SessionRow findSessionByRefresh(String refreshToken) { return sessions.stream().filter(row -> row.refreshToken().equals(refreshToken)).findFirst().orElse(null); }
        public SessionRow findSessionByAccess(String accessToken) { return sessions.stream().filter(row -> row.accessToken().equals(accessToken)).findFirst().orElse(null); }
        public int updateSession(long id, int status, int version, int oldVersion) { var row = sessions.stream().filter(v -> v.id() == id && v.version() == oldVersion).findFirst().orElse(null); if (row == null) return 0; sessions.set(sessions.indexOf(row), new SessionRow(row.id(), row.userId(), row.accessToken(), row.refreshToken(), status, version)); return 1; }
        public void insertPermission(long id, String appCode, String code, String name) { permissions.add(new PermissionRow(id, appCode, code, name)); }
        public List<PermissionRow> permissions(int limit) { return permissions; }
        public void insertDataScope(long id, long roleId, String type, String value) { scopes.add(new DataScopeRow(id, roleId, type, value)); }
        public List<DataScopeRow> dataScopes(long roleId) { return scopes.stream().filter(row -> row.roleId() == roleId).toList(); }
        public void insertApproval(long id, String no, String type, String businessNo) { approvals.add(new ApprovalRow(id, no, type, businessNo, 1, 0)); }
        public int completeApproval(String no, int status, int version) { var row = approvals.stream().filter(v -> v.approvalNo().equals(no) && v.version() == version).findFirst().orElse(null); if (row == null) return 0; approvals.set(approvals.indexOf(row), new ApprovalRow(row.id(), row.approvalNo(), row.businessType(), row.businessNo(), status, version + 1)); return 1; }
        public List<ApprovalRow> approvals(int limit) { return approvals; }
        public void insertOperationLog(long id, String operation, String targetNo) { logs.add(new OperationLogRow(id, operation, targetNo)); }
        public List<OperationLogRow> operationLogs(int limit) { return logs; }
        public void insertSecurityPolicy(long id, String code, String value) { policies.add(new SecurityPolicyRow(id, code, value, 0)); }
        public List<SecurityPolicyRow> securityPolicies(int limit) { return policies; }
    }
}
