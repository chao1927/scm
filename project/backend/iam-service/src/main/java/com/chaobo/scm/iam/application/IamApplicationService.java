package com.chaobo.scm.iam.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.domain.UserAggregate;
import com.chaobo.scm.iam.domain.SessionTokenPolicy;
import com.chaobo.scm.iam.application.mfa.MfaApplicationService;
import com.chaobo.scm.iam.infrastructure.jwt.IamJwtService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IamApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class IamApplicationService {

    /**
     * mapper（类型：{@code IamMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamMapper mapper;

    /**
     * jwtService（类型：{@code IamJwtService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamJwtService jwtService;

    /**
     * tokenClaimsProvider（类型：{@code IamTokenClaimsProvider}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IamTokenClaimsProvider tokenClaimsProvider;

    private final IamSessionMapper sessionMapper;

    private final TokenCachePort tokenCache;

    private final MfaApplicationService mfa;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 IamApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code IamMapper}
     */
    /**
     * 创建 IamApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code IamMapper}
     * @param jwtService 应用或外部协作依赖，类型为 {@code IamJwtService}
     * @param tokenClaimsProvider 业务或技术标识，类型为 {@code IamTokenClaimsProvider}
     */
    public IamApplicationService(IamMapper mapper, IamJwtService jwtService,
                                 IamTokenClaimsProvider tokenClaimsProvider,
                                 IamSessionMapper sessionMapper, TokenCachePort tokenCache) {
        this(mapper, jwtService, tokenClaimsProvider, sessionMapper, tokenCache, null);
    }

    @Autowired
    public IamApplicationService(IamMapper mapper, IamJwtService jwtService,
                                 IamTokenClaimsProvider tokenClaimsProvider,
                                 IamSessionMapper sessionMapper, TokenCachePort tokenCache,
                                 MfaApplicationService mfa) {
        this.mapper = mapper;
        this.jwtService = jwtService;
        this.tokenClaimsProvider = tokenClaimsProvider;
        this.sessionMapper = sessionMapper;
        this.tokenCache = tokenCache;
        this.mfa = mfa;
    }

    /**
     * 执行命令 {@code createUser}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param username 业务处理参数或成员，类型为 {@code String}
     * @param password 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code UserView}
     */
    @Transactional(rollbackFor = Exception.class)
    public UserView createUser(String username, String password) {
        if (mapper.findUserByUsername(username) != null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "用户名已存在");
        }
        long id = ids.incrementAndGet();
        mapper.insertUser(id, username, hash(password), 1, 0, 0);
        return new UserView(id, username, 1, 0);
    }

    /**
     * 处理当前类型职责中的操作 {@code login}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param username 业务处理参数或成员，类型为 {@code String}
     * @param password 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LoginResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult login(String username, String password) {
        return login(new LoginCommand(username, password, "SCM_WEB", "UNKNOWN_DEVICE"));
    }

    /**
     * 验证密码后根据用户 MFA 状态决定是返回挑战，还是直接签发会话。
     *
     * @param command 登录命令
     * @return 登录结果或仅包含 challengeId 的 MFA 挑战结果
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult login(LoginCommand command) {
        if (command == null || command.username() == null || command.username().isBlank()
                || command.password() == null || command.password().isBlank()
                || command.appCode() == null || command.appCode().isBlank()
                || command.deviceDigest() == null || command.deviceDigest().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "登录参数不完整");
        }
        String username = command.username();
        String password = command.password();
        var row = mapper.findUserByUsername(username);
        if (row == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户名或密码错误");
        }
        var user = toUser(row);
        int oldVersion = user.version();
        user.authenticate(hash(password));
        mapper.updateUser(user.id(), user.passwordHash(), user.status(), user.failedAttempts(), user.version(), oldVersion);
        long sessionId = ids.incrementAndGet();
        if (mfa != null && mfa.requiresMfa(user.id())) {
            var challenge = mfa.create(new MfaApplicationService.CreateCommand(user.id(),
                command.appCode(), sessionId, "LOGIN", command.deviceDigest(),
                "LOGIN:" + sessionId));
            mapper.insertOperationLog(ids.incrementAndGet(), "LOGIN_MFA_REQUIRED", username);
            return LoginResult.mfaRequired(user.id(), username, challenge.challengeNo(), sessionId);
        }
        return issueSession(user.id(), username, command.appCode(), sessionId);
    }

    /**
     * 完成已验证的 MFA 登录挑战，同一挑战只能得到同一个会话。
     *
     * @param command MFA 登录完成命令
     * @return 已认证会话
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult completeMfaLogin(MfaLoginCommand command) {
        if (command == null || command.challengeNo() == null || command.challengeNo().isBlank()
                || command.sessionId() <= 0 || command.deviceDigest() == null
                || command.deviceDigest().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "MFA登录参数不完整");
        }
        if (mfa == null) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "MFA服务未配置");
        }
        var challenge = mfa.requireVerifiedLoginChallenge(command.challengeNo(),
            command.sessionId(), command.deviceDigest());
        IamSessionMapper.SessionSnapshot existing = sessionMapper.find(command.sessionId());
        var user = mapper.findUserById(challenge.userId());
        if (user == null || user.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户不可登录");
        }
        if (existing != null) {
            if (existing.userId() != user.id()) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "MFA会话已属于其他用户");
            }
            return new LoginResult(existing.accessToken(), existing.refreshToken(), user.id(),
                user.username(), "AUTHENTICATED", null, existing.sessionId());
        }
        return issueSession(user.id(), user.username(), challenge.appCode(), command.sessionId());
    }

    private LoginResult issueSession(long userId, String username, String appCode, long sessionId) {
        IssuedToken access = issueToken(userId, username, appCode, "AT-" + sessionId, "ACCESS", 3600, true);
        IssuedToken refresh = issueToken(userId, username, appCode, "RT-" + sessionId, "REFRESH", 86400, false);
        sessionMapper.insert(new IamSessionMapper.SessionWrite(sessionId, userId, access.value(), refresh.value(),
                access.jti(), refresh.jti(), 0, access.expiresAt(), refresh.expiresAt()));
        tokenCache.store(new TokenCachePort.OnlineSession(sessionId, userId, access.jti(), refresh.jti(), 0,
                access.expiresAt(), refresh.expiresAt(), true));
        mapper.insertOperationLog(ids.incrementAndGet(), "LOGIN", username);
        return new LoginResult(access.value(), refresh.value(), userId, username,
            "AUTHENTICATED", null, sessionId);
    }

    /**
     * 处理当前类型职责中的操作 {@code refresh}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refreshToken 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LoginResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult refresh(String refreshToken) {
        IamJwtService.TokenClaims refreshClaims = verifyToken(refreshToken, "REFRESH");
        TokenCachePort.OnlineSession online = tokenCache.findByRefreshJti(refreshClaims.jti()).orElse(null);
        if (online == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "刷新令牌无效");
        }
        SessionTokenPolicy.RefreshDecision decision = SessionTokenPolicy.decideRefresh(
                online.active(), online.refreshJti(), refreshClaims.jti());
        if (decision == SessionTokenPolicy.RefreshDecision.REVOKED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "刷新令牌已撤销");
        }
        if (decision == SessionTokenPolicy.RefreshDecision.REPLAY) {
            sessionMapper.revoke(online.sessionId(), "REFRESH_TOKEN_REPLAY");
            tokenCache.revoke(online.sessionId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "检测到旧刷新令牌重放");
        }
        var user = mapper.findUserById(online.userId());
        if (user == null || user.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户不可登录");
        }
        long generation = online.generation() + 1;
        IssuedToken access = issueToken(user.id(), user.username(), "IAM", "AT-" + online.sessionId() + "-" + generation, "ACCESS", 3600, true);
        IssuedToken refresh = issueToken(user.id(), user.username(), "IAM", "RT-" + online.sessionId() + "-" + generation, "REFRESH", 86400, false);
        if (sessionMapper.rotate(online.sessionId(), refreshClaims.jti(), access.value(), refresh.value(),
                access.jti(), refresh.jti(), generation, access.expiresAt(), refresh.expiresAt()) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "会话刷新冲突");
        }
        TokenCachePort.OnlineSession replacement = new TokenCachePort.OnlineSession(online.sessionId(), user.id(),
                access.jti(), refresh.jti(), generation, access.expiresAt(), refresh.expiresAt(), true);
        TokenCachePort.RotationResult rotation = tokenCache.rotate(refreshClaims.jti(), replacement);
        if (rotation != TokenCachePort.RotationResult.ROTATED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "刷新令牌无效或已重放");
        }
        return new LoginResult(access.value(), refresh.value(), user.id(), user.username());
    }

    /**
     * 处理当前类型职责中的操作 {@code logout}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refreshToken 业务处理参数或成员，类型为 {@code String}
     */
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshToken) {
        IamJwtService.TokenClaims claims;
        try {
            claims = verifyToken(refreshToken, "REFRESH");
        } catch (BusinessException exception) {
            return;
        }
        TokenCachePort.OnlineSession online = tokenCache.findByRefreshJti(claims.jti()).orElse(null);
        if (online == null || !online.active()) {
            return;
        }
        sessionMapper.revoke(online.sessionId(), "LOGOUT");
        tokenCache.revoke(online.sessionId());
    }

    /**
     * 处理当前类型职责中的操作 {@code me}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param accessToken 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code UserView}
     */
    public UserView me(String accessToken) {
        IamJwtService.TokenClaims claims = verifyToken(accessToken, "ACCESS");
        TokenCachePort.OnlineSession session = tokenCache.findByAccessJti(claims.jti()).orElse(null);
        if (session == null || !session.active() || !claims.jti().equals(session.accessJti())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "访问令牌无效");
        }
        var user = mapper.findUserById(session.userId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return new UserView(user.id(), user.username(), user.status(), user.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code users}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamMapper.UserRow>}
     */
    public List<IamMapper.UserRow> users(int limit) {
        return mapper.users(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 处理当前类型职责中的操作 {@code roles}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamMapper.RoleRow>}
     */
    public List<IamMapper.RoleRow> roles(int limit) {
        return mapper.roles(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /** 返回脱敏的会话治理读模型。 */
    public List<IamSessionMapper.SessionGovernanceRow> sessions(int limit) {
        return sessionMapper.list(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    public List<IamMapper.RoleGrantRow> roleGrants(int limit) {
        return mapper.roleGrants(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    public List<IamMapper.UserRoleRow> userRoles(int limit) {
        return mapper.userRoles(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 执行命令 {@code createRole}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code RoleView}
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleView createRole(String code, String name) {
        var existed = mapper.findRole(code);
        if (existed != null) {
            return new RoleView(existed.id(), existed.roleCode(), existed.roleName(), existed.status(), true);
        }
        long id = ids.incrementAndGet();
        mapper.insertRole(id, code, name, 1, 0);
        return new RoleView(id, code, name, 1, false);
    }

    /**
     * 执行命令 {@code bindUserRole}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param roleId 业务或技术标识，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindUserRole(long userId, long roleId) {
        mapper.bindUserRole(userId, roleId);
        mapper.insertOperationLog(ids.incrementAndGet(), "BIND_USER_ROLE", userId + ":" + roleId);
    }

    /**
     * 处理当前类型职责中的操作 {@code grantRolePermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param roleId 业务或技术标识，类型为 {@code long}
     * @param permissionCode 可追踪业务编码，类型为 {@code String}
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantRolePermission(long roleId, String permissionCode) {
        mapper.grantRolePermission(roleId, permissionCode);
        mapper.insertOperationLog(ids.incrementAndGet(), "GRANT_ROLE_PERMISSION", roleId + ":" + permissionCode);
    }

    /**
     * 执行命令 {@code createPermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPermission(String appCode, String code, String name) {
        mapper.insertPermission(ids.incrementAndGet(), appCode, code, name);
    }

    /**
     * 处理当前类型职责中的操作 {@code permissions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamMapper.PermissionRow>}
     */
    public List<IamMapper.PermissionRow> permissions(int limit) {
        return mapper.permissions(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 执行命令 {@code createDataScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param roleId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param value 业务处理参数或成员，类型为 {@code String}
     */
    @Transactional(rollbackFor = Exception.class)
    public void createDataScope(long roleId, String type, String value) {
        mapper.insertDataScope(ids.incrementAndGet(), roleId, type, value);
    }

    /**
     * 处理当前类型职责中的操作 {@code dataScopes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param roleId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamMapper.DataScopeRow>}
     */
    public List<IamMapper.DataScopeRow> dataScopes(long roleId) {
        return mapper.dataScopes(roleId);
    }

    /**
     * 执行命令 {@code createApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code ApprovalView}
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalView createApproval(String type, String businessNo) {
        long id = ids.incrementAndGet();
        String no = "APR" + id;
        mapper.insertApproval(id, no, type, businessNo);
        return new ApprovalView(no, type, businessNo, 1, 0);
    }

    /**
     * 执行命令 {@code completeApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param approvalNo 可追踪业务编码，类型为 {@code String}
     * @param approved 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeApproval(String approvalNo, boolean approved, int version) {
        if (mapper.completeApproval(approvalNo, approved ? COMPLETE_APPROVAL_VALUE_2 : COMPLETE_APPROVAL_VALUE_3, version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "审批版本冲突");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code approvals}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamMapper.ApprovalRow>}
     */
    public List<IamMapper.ApprovalRow> approvals(int limit) {
        return mapper.approvals(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 处理当前类型职责中的操作 {@code operationLogs}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamMapper.OperationLogRow>}
     */
    public List<IamMapper.OperationLogRow> operationLogs(int limit) {
        return mapper.operationLogs(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 执行命令 {@code createSecurityPolicy}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param value 业务处理参数或成员，类型为 {@code String}
     */
    @Transactional(rollbackFor = Exception.class)
    public void createSecurityPolicy(String code, String value) {
        mapper.insertSecurityPolicy(ids.incrementAndGet(), code, value);
    }

    /**
     * 处理当前类型职责中的操作 {@code securityPolicies}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IamMapper.SecurityPolicyRow>}
     */
    public List<IamMapper.SecurityPolicyRow> securityPolicies(int limit) {
        return mapper.securityPolicies(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 处理当前类型职责中的操作 {@code hash}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param password 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "密码不能为空");
        }
        return "HASH:" + password;
    }

    /**
     * 转换数据模型 {@code toUser}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code IamMapper.UserRow}
     * @return 转换数据模型的结果，类型为 {@code UserAggregate}
     */
    private static UserAggregate toUser(IamMapper.UserRow row) {
        return new UserAggregate(row.id(), row.username(), row.passwordHash(), row.status(), row.failedAttempts(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code issueToken}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param username 业务处理参数或成员，类型为 {@code String}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param jti 业务处理参数或成员，类型为 {@code String}
     * @param tokenType 业务处理参数或成员，类型为 {@code String}
     * @param secondsToLive 业务处理参数或成员，类型为 {@code long}
     * @param includeAuthorization 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private IssuedToken issueToken(long userId, String username, String appCode, String jti, String tokenType, long secondsToLive, boolean includeAuthorization) {
        long now = Instant.now().getEpochSecond();
        IamTokenClaimsProvider.PermissionClaims authorization = includeAuthorization ? tokenClaimsProvider.claimsFor(userId) : new IamTokenClaimsProvider.PermissionClaims(Set.of(), Map.of());
        long expiresAt = now + secondsToLive;
        return new IssuedToken(jwtService.issue(new IamJwtService.TokenClaims(String.valueOf(userId), username,
                appCode, jti, tokenType, now, expiresAt, authorization.permissions(), authorization.dataScopes())),
                jti, expiresAt);
    }

    private IamJwtService.TokenClaims verifyToken(String token, String expectedType) {
        try {
            IamJwtService.TokenClaims claims = jwtService.verify(token);
            if (!expectedType.equals(claims.tokenType())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Token类型无效");
            }
            return claims;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Token签名或有效期无效");
        }
    }

    private record IssuedToken(String value, String jti, long expiresAt) {
    }

    /**
     * UserView。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record UserView(long id, String username, int status, int version) {
    }

    /**
     * LoginResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record LoginResult(String accessToken, String refreshToken, long userId, String username,
                              String status, String challengeNo, Long sessionId) {

        public LoginResult(String accessToken, String refreshToken, long userId, String username) {
            this(accessToken, refreshToken, userId, username, "AUTHENTICATED", null, null);
        }

        public static LoginResult mfaRequired(long userId, String username,
                                              String challengeNo, long sessionId) {
            return new LoginResult(null, null, userId, username, "MFA_REQUIRED",
                challengeNo, sessionId);
        }
    }

    /** 密码登录命令，设备摘要用于绑定 MFA 挑战。 */
    public record LoginCommand(String username, String password, String appCode,
                               String deviceDigest) {
    }

    /** 已验证 MFA 挑战的会话签发命令。 */
    public record MfaLoginCommand(String challengeNo, long sessionId, String deviceDigest) {
    }

    /**
     * RoleView。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RoleView(long id, String code, String name, int status, boolean duplicated) {
    }

    /**
     * ApprovalView。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ApprovalView(String approvalNo, String businessType, String businessNo, int status, int version) {
    }

    /**
     * 业务常量 {@code COMPLETE_APPROVAL_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int COMPLETE_APPROVAL_VALUE_2 = 2;

    /**
     * 业务常量 {@code COMPLETE_APPROVAL_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int COMPLETE_APPROVAL_VALUE_3 = 3;
}
