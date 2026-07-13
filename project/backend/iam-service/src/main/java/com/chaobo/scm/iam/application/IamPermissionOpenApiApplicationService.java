package com.chaobo.scm.iam.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.infrastructure.jwt.IamJwtService;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class IamPermissionOpenApiApplicationService {
    private final IamMapper iamMapper;
    private final IamPermissionOpenApiMapper mapper;
    private final IamJwtService jwtService;
    private final AtomicLong eventIds = new AtomicLong(System.currentTimeMillis());

    public IamPermissionOpenApiApplicationService(IamMapper iamMapper, IamPermissionOpenApiMapper mapper) {
        this(iamMapper, mapper, new IamJwtService("01234567890123456789012345678901"));
    }

    public IamPermissionOpenApiApplicationService(IamMapper iamMapper, IamPermissionOpenApiMapper mapper,
                                                  IamJwtService jwtService) {
        this.iamMapper = iamMapper;
        this.mapper = mapper;
        this.jwtService = jwtService;
    }

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

    @Transactional
    public PermissionSnapshot snapshot(String accessToken, String appCode) {
        TokenValidationResult token = validateToken(new TokenValidationCommand(accessToken));
        String resolvedApp = appCode == null || appCode.isBlank() ? token.appCode() : appCode;
        IamPermissionOpenApiMapper.PermissionSnapshotRow existing =
                mapper.findSnapshot(token.userId(), resolvedApp);
        if (existing != null && existing.status() == 1) {
            return new PermissionSnapshot(token.userId(), resolvedApp, existing.rolePayload(),
                    existing.permissionPayload(), existing.dataScopePayload(), existing.version(), true);
        }
        List<IamPermissionOpenApiMapper.RoleGrantRow> roles = mapper.roleGrants(token.userId());
        List<IamPermissionOpenApiMapper.PermissionGrantRow> permissions = mapper.permissionGrants(token.userId());
        List<IamPermissionOpenApiMapper.DataScopeGrantRow> scopes = mapper.dataScopeGrants(token.userId());
        IamPermissionOpenApiMapper.PermissionSnapshotRow rebuilt =
                new IamPermissionOpenApiMapper.PermissionSnapshotRow(token.userId(), resolvedApp,
                        roles.stream().map(IamPermissionOpenApiMapper.RoleGrantRow::roleCode).collect(Collectors.joining(",")),
                        permissions.stream().map(IamPermissionOpenApiMapper.PermissionGrantRow::permissionCode).collect(Collectors.joining(",")),
                        scopes.stream().map(row -> row.scopeType() + ":" + row.scopeValue()).collect(Collectors.joining(",")),
                        1, existing == null ? 1 : existing.version() + 1, LocalDateTime.now());
        if (existing == null) {
            mapper.insertSnapshot(rebuilt);
        } else {
            mapper.updateSnapshot(rebuilt);
        }
        return new PermissionSnapshot(token.userId(), resolvedApp, rebuilt.rolePayload(), rebuilt.permissionPayload(),
                rebuilt.dataScopePayload(), rebuilt.version(), false);
    }

    public PermissionCheckResult checkPermission(PermissionCheckCommand command) {
        PermissionSnapshot snapshot = snapshot(command.accessToken(), command.appCode());
        boolean allowed = List.of(snapshot.permissionPayload().split(",")).contains(command.permissionCode());
        return new PermissionCheckResult(allowed, allowed ? null : "PERMISSION_DENIED", snapshot);
    }

    public DataScopeResolveResult resolveDataScope(DataScopeResolveCommand command) {
        PermissionSnapshot snapshot = snapshot(command.accessToken(), command.appCode());
        String prefix = command.scopeType() + ":";
        List<String> values = List.of(snapshot.dataScopePayload().split(",")).stream()
                .filter(item -> item.startsWith(prefix))
                .map(item -> item.substring(prefix.length()))
                .filter(item -> !item.isBlank())
                .toList();
        return new DataScopeResolveResult(command.scopeType(), values, snapshot.version());
    }

    @Transactional
    public void invalidateUserSnapshots(long userId, String reason) {
        mapper.invalidateSnapshots(userId);
        mapper.insertOutbox(new IamPermissionOpenApiMapper.OutboxEventRow(eventIds.incrementAndGet(),
                "PermissionSnapshotInvalidated", String.valueOf(userId), reason == null ? "" : reason,
                1, LocalDateTime.now()));
    }

    private IamJwtService.TokenClaims verifyAccessToken(String accessToken) {
        try {
            IamJwtService.TokenClaims claims = jwtService.verify(accessToken);
            if (!"ACCESS".equals(claims.tokenType())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Token类型无效");
            }
            return claims;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Token签名无效");
        }
    }

    public record TokenValidationCommand(String accessToken) {}
    public record TokenValidationResult(boolean valid, long userId, String username, String appCode,
                                        long permissionVersion) {}
    public record PermissionSnapshot(long userId, String appCode, String rolePayload, String permissionPayload,
                                     String dataScopePayload, long version, boolean cacheHit) {}
    public record PermissionCheckCommand(String accessToken, String appCode, String permissionCode) {}
    public record PermissionCheckResult(boolean allowed, String denyReason, PermissionSnapshot snapshot) {}
    public record DataScopeResolveCommand(String accessToken, String appCode, String scopeType) {}
    public record DataScopeResolveResult(String scopeType, List<String> scopeValues, long permissionVersion) {}
}
