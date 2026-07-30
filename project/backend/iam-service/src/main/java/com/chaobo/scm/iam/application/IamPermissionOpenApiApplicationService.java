package com.chaobo.scm.iam.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.infrastructure.jwt.IamJwtService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * IamPermissionOpenApiApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class IamPermissionOpenApiApplicationService {

    /**
     * iamMapper（类型：{@code IamMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamMapper iamMapper;

    /**
     * mapper（类型：{@code IamPermissionOpenApiMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamPermissionOpenApiMapper mapper;

    /**
     * jwtService（类型：{@code IamJwtService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final IamJwtService jwtService;

    /**
     * eventIds（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong eventIds = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 IamPermissionOpenApiApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param iamMapper 持久化访问依赖，类型为 {@code IamMapper}
     * @param mapper 持久化访问依赖，类型为 {@code IamPermissionOpenApiMapper}
     */
    public IamPermissionOpenApiApplicationService(IamMapper iamMapper, IamPermissionOpenApiMapper mapper) {
        this(iamMapper, mapper, new IamJwtService("01234567890123456789012345678901"));
    }

    /**
     * 创建 IamPermissionOpenApiApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param iamMapper 持久化访问依赖，类型为 {@code IamMapper}
     * @param mapper 持久化访问依赖，类型为 {@code IamPermissionOpenApiMapper}
     * @param jwtService 应用或外部协作依赖，类型为 {@code IamJwtService}
     */
    @Autowired
    public IamPermissionOpenApiApplicationService(IamMapper iamMapper, IamPermissionOpenApiMapper mapper, IamJwtService jwtService) {
        this.iamMapper = iamMapper;
        this.mapper = mapper;
        this.jwtService = jwtService;
    }

    /**
     * 校验业务约束 {@code validateToken}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code TokenValidationCommand}
     * @return 校验业务约束的结果，类型为 {@code TokenValidationResult}
     */
    public TokenValidationResult validateToken(TokenValidationCommand command) {
        IamJwtService.TokenClaims claims = verifyAccessToken(command.accessToken());
        IamMapper.SessionRow session = iamMapper.findSessionByAccess(command.accessToken());
        if (session == null || session.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Token已撤销");
        }
        IamMapper.UserRow user = iamMapper.findUserById(session.userId());
        if (user == null || user.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户不可用");
        }
        return new TokenValidationResult(true, user.id(), user.username(), claims.appCode(), user.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param accessToken 业务处理参数或成员，类型为 {@code String}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PermissionSnapshot}
     */
    @Transactional(rollbackFor = Exception.class)
    public PermissionSnapshot snapshot(String accessToken, String appCode) {
        TokenValidationResult token = validateToken(new TokenValidationCommand(accessToken));
        String resolvedApp = appCode == null || appCode.isBlank() ? token.appCode() : appCode;
        IamPermissionOpenApiMapper.PermissionSnapshotRow existing = mapper.findSnapshot(token.userId(), resolvedApp);
        if (existing != null && existing.status() == 1) {
            return new PermissionSnapshot(token.userId(), resolvedApp, existing.rolePayload(), existing.permissionPayload(), existing.dataScopePayload(), existing.version(), true);
        }
        List<IamPermissionOpenApiMapper.RoleGrantRow> roles = mapper.roleGrants(token.userId());
        List<IamPermissionOpenApiMapper.PermissionGrantRow> permissions = mapper.permissionGrants(token.userId());
        List<IamPermissionOpenApiMapper.DataScopeGrantRow> scopes = mapper.dataScopeGrants(token.userId());
        IamPermissionOpenApiMapper.PermissionSnapshotRow rebuilt = new IamPermissionOpenApiMapper.PermissionSnapshotRow(token.userId(), resolvedApp, roles.stream().map(IamPermissionOpenApiMapper.RoleGrantRow::roleCode).collect(Collectors.joining(",")), permissions.stream().map(IamPermissionOpenApiMapper.PermissionGrantRow::permissionCode).collect(Collectors.joining(",")), scopes.stream().map(row -> row.scopeType() + ":" + row.scopeValue()).collect(Collectors.joining(",")), 1, existing == null ? 1 : existing.version() + 1, LocalDateTime.now());
        if (existing == null) {
            mapper.insertSnapshot(rebuilt);
        } else {
            mapper.updateSnapshot(rebuilt);
        }
        return new PermissionSnapshot(token.userId(), resolvedApp, rebuilt.rolePayload(), rebuilt.permissionPayload(), rebuilt.dataScopePayload(), rebuilt.version(), false);
    }

    /**
     * 校验业务约束 {@code checkPermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code PermissionCheckCommand}
     * @return 校验业务约束的结果，类型为 {@code PermissionCheckResult}
     */
    public PermissionCheckResult checkPermission(PermissionCheckCommand command) {
        PermissionSnapshot snapshot = snapshot(command.accessToken(), command.appCode());
        boolean allowed = List.of(snapshot.permissionPayload().split(",")).contains(command.permissionCode());
        return new PermissionCheckResult(allowed, allowed ? null : "PERMISSION_DENIED", snapshot);
    }

    /**
     * 处理当前类型职责中的操作 {@code resolveDataScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code DataScopeResolveCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code DataScopeResolveResult}
     */
    public DataScopeResolveResult resolveDataScope(DataScopeResolveCommand command) {
        PermissionSnapshot snapshot = snapshot(command.accessToken(), command.appCode());
        String prefix = command.scopeType() + ":";
        List<String> values = List.of(snapshot.dataScopePayload().split(",")).stream().filter(item -> item.startsWith(prefix)).map(item -> item.substring(prefix.length())).filter(item -> !item.isBlank()).toList();
        return new DataScopeResolveResult(command.scopeType(), values, snapshot.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code invalidateUserSnapshots}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Transactional(rollbackFor = Exception.class)
    public void invalidateUserSnapshots(long userId, String reason) {
        mapper.invalidateSnapshots(userId);
        mapper.insertOutbox(new IamPermissionOpenApiMapper.OutboxEventRow(eventIds.incrementAndGet(), "PermissionSnapshotInvalidated", String.valueOf(userId), reason == null ? "" : reason, 1, LocalDateTime.now()));
    }

    /**
     * 处理当前类型职责中的操作 {@code verifyAccessToken}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param accessToken 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamJwtService.TokenClaims}
     */
    private IamJwtService.TokenClaims verifyAccessToken(String accessToken) {
        try {
            IamJwtService.TokenClaims claims = jwtService.verify(accessToken);
            if (!ACCESS.equals(claims.tokenType())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Token类型无效");
            }
            return claims;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Token签名无效");
        }
    }

    /**
     * TokenValidationCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record TokenValidationCommand(String accessToken) {
    }

    /**
     * TokenValidationResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record TokenValidationResult(boolean valid, long userId, String username, String appCode, long permissionVersion) {
    }

    /**
     * PermissionSnapshot。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PermissionSnapshot(long userId, String appCode, String rolePayload, String permissionPayload, String dataScopePayload, long version, boolean cacheHit) {
    }

    /**
     * PermissionCheckCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PermissionCheckCommand(String accessToken, String appCode, String permissionCode) {
    }

    /**
     * PermissionCheckResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PermissionCheckResult(boolean allowed, String denyReason, PermissionSnapshot snapshot) {
    }

    /**
     * DataScopeResolveCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DataScopeResolveCommand(String accessToken, String appCode, String scopeType) {
    }

    /**
     * DataScopeResolveResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DataScopeResolveResult(String scopeType, List<String> scopeValues, long permissionVersion) {
    }

    /**
     * 业务常量 {@code ACCESS}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String ACCESS = "ACCESS";
}
