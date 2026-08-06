package com.chaobo.scm.iam.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.integration.IamCollaborationApi;
import com.chaobo.scm.iam.infrastructure.persistence.IamCollaborationMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import com.chaobo.scm.iam.infrastructure.persistence.IamPermissionOpenApiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/** 幂等替换用户级供应商数据范围，并使权限快照立即失效。 */
@Service
public class IamCollaborationApplicationService {

    private static final String UPDATE_SCOPE = "UPDATE_SUPPLIER_SCOPE";
    private final IamCollaborationMapper collaboration;
    private final IamMapper users;
    private final IamPermissionOpenApiMapper permissions;
    private final AtomicLong eventIds = new AtomicLong(System.currentTimeMillis());

    public IamCollaborationApplicationService(IamCollaborationMapper collaboration, IamMapper users,
                                             IamPermissionOpenApiMapper permissions) {
        this.collaboration = collaboration;
        this.users = users;
        this.permissions = permissions;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSupplierScope(IamCollaborationApi.UpdateSupplierScopeCommand command) {
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw invalid("Dubbo 命令必须提供幂等键");
        }
        if (command.userId() <= 0 || command.supplierIds() == null || command.reason() == null
                || command.reason().isBlank()
                || command.supplierIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw invalid("用户供应商数据范围命令不完整");
        }
        String fingerprint = fingerprint(command.userId(), command.supplierIds(), command.reason());
        IamCollaborationMapper.Receipt duplicate = collaboration.findReceipt(command.idempotencyKey());
        if (duplicate != null) {
            if (!duplicate.commandType().equals(UPDATE_SCOPE)
                    || !duplicate.requestFingerprint().equals(fingerprint)) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT,
                        "Dubbo 幂等键已用于不同 IAM 命令");
            }
            return;
        }
        if (users.findUserById(command.userId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "IAM 用户不存在");
        }
        collaboration.deleteSupplierScopes(command.userId());
        command.supplierIds().stream().sorted().forEach(
                supplierId -> collaboration.insertSupplierScope(command.userId(), supplierId));
        permissions.invalidateSnapshots(command.userId());
        long eventId = eventIds.incrementAndGet();
        permissions.insertOutbox(new IamPermissionOpenApiMapper.OutboxEventRow(eventId,
                "IamSupplierScopeUpdated", Long.toString(command.userId()),
                "{\"userId\":" + command.userId() + ",\"supplierIds\":\""
                        + sorted(command.supplierIds()) + "\"}", 1, LocalDateTime.now()));
        users.insertOperationLog(eventIds.incrementAndGet(), "UPDATE_SUPPLIER_SCOPE",
                command.userId() + ":" + command.reason());
        collaboration.insertReceipt(new IamCollaborationMapper.Receipt(command.idempotencyKey(),
                UPDATE_SCOPE, fingerprint, Long.toString(command.userId())));
    }

    private static String fingerprint(long userId, Set<Long> supplierIds, String reason) {
        return userId + "|" + sorted(supplierIds) + "|" + reason.trim();
    }

    private static String sorted(Set<Long> supplierIds) {
        return supplierIds.stream().sorted(Comparator.naturalOrder()).map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
