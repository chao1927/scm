package com.chaobo.scm.iam.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.domain.SessionTokenAggregate;
import com.chaobo.scm.iam.domain.UserAggregate;
import com.chaobo.scm.iam.infrastructure.jwt.IamJwtService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IamApplicationService {
    private final IamMapper mapper;
    private final IamJwtService jwtService;
    private final IamTokenClaimsProvider tokenClaimsProvider;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public IamApplicationService(IamMapper mapper) {
        this(mapper, new IamJwtService("01234567890123456789012345678901"));
    }

    public IamApplicationService(IamMapper mapper, IamJwtService jwtService) {
        this(mapper, jwtService, userId -> new IamTokenClaimsProvider.PermissionClaims(Set.of(), Map.of()));
    }

    @Autowired
    public IamApplicationService(IamMapper mapper, IamJwtService jwtService,
                                 IamTokenClaimsProvider tokenClaimsProvider) {
        this.mapper = mapper;
        this.jwtService = jwtService;
        this.tokenClaimsProvider = tokenClaimsProvider;
    }

    @Transactional
    public UserView createUser(String username, String password) {
        if (mapper.findUserByUsername(username) != null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "用户名已存在");
        }
        long id = ids.incrementAndGet();
        mapper.insertUser(id, username, hash(password), 1, 0, 0);
        return new UserView(id, username, 1, 0);
    }

    @Transactional
    public LoginResult login(String username, String password) {
        var row = mapper.findUserByUsername(username);
        if (row == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户名或密码错误");
        }
        var user = toUser(row);
        int oldVersion = user.version();
        user.authenticate(hash(password));
        mapper.updateUser(user.id(), user.passwordHash(), user.status(), user.failedAttempts(), user.version(), oldVersion);
        long sessionId = ids.incrementAndGet();
        String access = issueToken(user.id(), username, "IAM", "AT-" + sessionId, "ACCESS", 3600, true);
        String refresh = issueToken(user.id(), username, "IAM", "RT-" + sessionId, "REFRESH", 86400, false);
        mapper.insertSession(sessionId, user.id(), access, refresh, 1, 0);
        mapper.insertOperationLog(ids.incrementAndGet(), "LOGIN", username);
        return new LoginResult(access, refresh, user.id(), username);
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {
        jwtService.verify(refreshToken);
        var row = mapper.findSessionByRefresh(refreshToken);
        if (row == null || row.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "刷新令牌无效");
        }
        var user = mapper.findUserById(row.userId());
        if (user == null || user.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户不可登录");
        }
        long sessionId = ids.incrementAndGet();
        String access = issueToken(user.id(), user.username(), "IAM", "AT-" + sessionId, "ACCESS", 3600, true);
        String refresh = issueToken(user.id(), user.username(), "IAM", "RT-" + sessionId, "REFRESH", 86400, false);
        mapper.insertSession(sessionId, user.id(), access, refresh, 1, 0);
        return new LoginResult(access, refresh, user.id(), user.username());
    }

    @Transactional
    public void logout(String refreshToken) {
        var row = mapper.findSessionByRefresh(refreshToken);
        if (row == null) {
            return;
        }
        var session = new SessionTokenAggregate(row.id(), row.userId(), row.accessToken(), row.refreshToken(), row.status(), row.version());
        int oldVersion = session.version();
        session.logout();
        mapper.updateSession(session.id(), session.status(), session.version(), oldVersion);
    }

    public UserView me(String accessToken) {
        jwtService.verify(accessToken);
        var session = mapper.findSessionByAccess(accessToken);
        if (session == null || session.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "访问令牌无效");
        }
        var user = mapper.findUserById(session.userId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return new UserView(user.id(), user.username(), user.status(), user.version());
    }

    public List<IamMapper.UserRow> users(int limit) {
        return mapper.users(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    public List<IamMapper.RoleRow> roles(int limit) {
        return mapper.roles(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    @Transactional
    public RoleView createRole(String code, String name) {
        var existed = mapper.findRole(code);
        if (existed != null) {
            return new RoleView(existed.id(), existed.roleCode(), existed.roleName(), existed.status(), true);
        }
        long id = ids.incrementAndGet();
        mapper.insertRole(id, code, name, 1, 0);
        return new RoleView(id, code, name, 1, false);
    }

    @Transactional
    public void bindUserRole(long userId, long roleId) {
        mapper.bindUserRole(userId, roleId);
        mapper.insertOperationLog(ids.incrementAndGet(), "BIND_USER_ROLE", userId + ":" + roleId);
    }

    @Transactional
    public void grantRolePermission(long roleId, String permissionCode) {
        mapper.grantRolePermission(roleId, permissionCode);
        mapper.insertOperationLog(ids.incrementAndGet(), "GRANT_ROLE_PERMISSION", roleId + ":" + permissionCode);
    }

    @Transactional
    public void createPermission(String appCode, String code, String name) {
        mapper.insertPermission(ids.incrementAndGet(), appCode, code, name);
    }

    public List<IamMapper.PermissionRow> permissions(int limit) {
        return mapper.permissions(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    @Transactional
    public void createDataScope(long roleId, String type, String value) {
        mapper.insertDataScope(ids.incrementAndGet(), roleId, type, value);
    }

    public List<IamMapper.DataScopeRow> dataScopes(long roleId) {
        return mapper.dataScopes(roleId);
    }

    @Transactional
    public ApprovalView createApproval(String type, String businessNo) {
        long id = ids.incrementAndGet();
        String no = "APR" + id;
        mapper.insertApproval(id, no, type, businessNo);
        return new ApprovalView(no, type, businessNo, 1, 0);
    }

    @Transactional
    public void completeApproval(String approvalNo, boolean approved, int version) {
        if (mapper.completeApproval(approvalNo, approved ? 2 : 3, version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "审批版本冲突");
        }
    }

    public List<IamMapper.ApprovalRow> approvals(int limit) {
        return mapper.approvals(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    public List<IamMapper.OperationLogRow> operationLogs(int limit) {
        return mapper.operationLogs(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    @Transactional
    public void createSecurityPolicy(String code, String value) {
        mapper.insertSecurityPolicy(ids.incrementAndGet(), code, value);
    }

    public List<IamMapper.SecurityPolicyRow> securityPolicies(int limit) {
        return mapper.securityPolicies(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    private static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "密码不能为空");
        }
        return "HASH:" + password;
    }

    private static UserAggregate toUser(IamMapper.UserRow row) {
        return new UserAggregate(row.id(), row.username(), row.passwordHash(), row.status(), row.failedAttempts(), row.version());
    }

    private String issueToken(long userId, String username, String appCode, String jti, String tokenType,
                              long secondsToLive, boolean includeAuthorization) {
        long now = Instant.now().getEpochSecond();
        IamTokenClaimsProvider.PermissionClaims authorization = includeAuthorization
                ? tokenClaimsProvider.claimsFor(userId)
                : new IamTokenClaimsProvider.PermissionClaims(Set.of(), Map.of());
        return jwtService.issue(new IamJwtService.TokenClaims(String.valueOf(userId), username, appCode, jti,
                tokenType, now, now + secondsToLive, authorization.permissions(), authorization.dataScopes()));
    }

    public record UserView(long id, String username, int status, int version) {}
    public record LoginResult(String accessToken, String refreshToken, long userId, String username) {}
    public record RoleView(long id, String code, String name, int status, boolean duplicated) {}
    public record ApprovalView(String approvalNo, String businessType, String businessNo, int status, int version) {}
}
