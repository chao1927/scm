package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.domain.StockFreezeAggregate;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 冻结单应用服务。
 *
 * <p>服务在一个本地事务中协调冻结单、库存账户、不可变流水、Outbox、幂等回执和操作审计。
 * IAM 审批结果可以复用审批命令进入该服务，但审批系统不拥有库存数量的修改权。
 *
 * @author SCM Team
 */
@Service
public class StockFreezeApplicationService {

    private static final String AGGREGATE_TYPE = "StockFreeze";
    private static final String APPROVAL_RESULT_APPROVE = "APPROVE";
    private static final String APPROVAL_RESULT_REJECT = "REJECT";
    private final InventoryWorkflowRepository repository;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public StockFreezeApplicationService(InventoryWorkflowRepository repository) {
        this.repository = repository;
    }

    /**
     * 返回单据所属数据范围，供接口层在执行写命令前校验货主和仓库权限。
     */
    public InventoryScope scope(String freezeNo) {
        StockFreezeAggregate freeze = requiredFreeze(freezeNo);
        return new InventoryScope(freeze.ownerId(), freeze.warehouseId());
    }

    /**
     * 创建冻结草稿并可选自动提交审批；此阶段不改变库存账户。
     */
    @Transactional(rollbackFor = Exception.class)
    public FreezeResult create(CreateFreezeCommand command) {
        String fingerprint = fingerprint(
                "CREATE", command.ownerId(), command.warehouseId(), command.sku(),
                command.batchNo(), command.quantity(), command.reason(), command.sourceSystem(),
                command.sourceNo(), command.autoSubmit());
        FreezeResult duplicate = duplicate(command.idempotencyKey(), "CREATE_FREEZE", fingerprint);
        if (duplicate != null) {
            return duplicate;
        }
        InventoryAccountAggregate account = requiredAccount(
                command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        long id = ids.incrementAndGet();
        StockFreezeAggregate freeze = StockFreezeAggregate.create(
                id, "FRZ" + id, account.id(), command.ownerId(), command.warehouseId(),
                command.sku(), command.batchNo(), command.quantity(), command.reason(),
                command.sourceSystem(), command.sourceNo(), command.operatorId());
        if (command.autoSubmit()) {
            freeze.submit();
        }
        repository.insertFreeze(freeze);
        repository.appendOutbox(
                "StockFreezeCreated",
                AGGREGATE_TYPE,
                freeze.id(),
                freeze.freezeNo(),
                freezePayload(freeze, account));
        repository.appendAudit(audit(
                command.operatorId(), "CREATE_FREEZE", command.reason(), freeze,
                "{}", freezePayload(freeze, account),
                command.requestId(), command.idempotencyKey()));
        repository.saveReceipt(
                command.idempotencyKey(), "CREATE_FREEZE", fingerprint, freeze.freezeNo());
        return view(freeze, false);
    }

    /**
     * 审批冻结单。审批通过时账户冻结和流水必须与单据状态同时提交。
     */
    @Transactional(rollbackFor = Exception.class)
    public FreezeResult approve(ApproveFreezeCommand command) {
        String fingerprint = fingerprint(
                "APPROVE", command.freezeNo(), command.result(), command.approvalNo(),
                command.operatorId(), command.version());
        FreezeResult duplicate = duplicate(command.idempotencyKey(), "APPROVE_FREEZE", fingerprint);
        if (duplicate != null) {
            return duplicate;
        }
        StockFreezeAggregate freeze = requiredFreeze(command.freezeNo());
        requireVersion(freeze.version(), command.version(), "冻结单版本冲突");
        int oldFreezeVersion = freeze.version();
        InventoryAccountAggregate account = repository.findAccountById(freeze.accountId());
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        String before = accountSnapshot(account);
        if (APPROVAL_RESULT_APPROVE.equalsIgnoreCase(command.result())) {
            int oldAccountVersion = account.version();
            freeze.approve(command.approvalNo(), command.operatorId());
            account.freeze(freeze.freezeQty());
            repository.saveAccount(account, oldAccountVersion);
            repository.appendLedger(
                    account.id(), "FREEZE", freeze.freezeQty().negate(),
                    freeze.sourceSystem(), freeze.freezeNo());
            repository.appendOutbox(
                    "StockFrozen", AGGREGATE_TYPE, freeze.id(), freeze.freezeNo(),
                    freezePayload(freeze, account));
        } else if (APPROVAL_RESULT_REJECT.equalsIgnoreCase(command.result())) {
            freeze.reject(command.approvalNo(), command.operatorId());
            repository.appendOutbox(
                    "StockFreezeRejected", AGGREGATE_TYPE, freeze.id(), freeze.freezeNo(),
                    freezePayload(freeze, account));
        } else {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "审批结果只允许 APPROVE 或 REJECT");
        }
        repository.saveFreeze(freeze, oldFreezeVersion);
        repository.appendAudit(audit(
                command.operatorId(), "APPROVE_FREEZE",
                command.result() + ":" + command.approvalNo(), freeze,
                before, accountSnapshot(account),
                command.requestId(), command.idempotencyKey()));
        repository.saveReceipt(
                command.idempotencyKey(), "APPROVE_FREEZE", fingerprint, freeze.freezeNo());
        return view(freeze, false);
    }

    /**
     * 从指定冻结单解冻，不能使用账户维度直接解冻，以保证来源和剩余量可追溯。
     */
    @Transactional(rollbackFor = Exception.class)
    public FreezeResult unfreeze(UnfreezeCommand command) {
        String fingerprint = fingerprint(
                "UNFREEZE", command.freezeNo(), command.quantity(), command.reason(),
                command.operatorId(), command.version());
        FreezeResult duplicate = duplicate(command.idempotencyKey(), "UNFREEZE_STOCK", fingerprint);
        if (duplicate != null) {
            return duplicate;
        }
        StockFreezeAggregate freeze = requiredFreeze(command.freezeNo());
        requireVersion(freeze.version(), command.version(), "冻结单版本冲突");
        InventoryAccountAggregate account = repository.findAccountById(freeze.accountId());
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        int oldFreezeVersion = freeze.version();
        int oldAccountVersion = account.version();
        String before = accountSnapshot(account);
        freeze.unfreeze(command.quantity());
        account.unfreeze(command.quantity());
        repository.saveAccount(account, oldAccountVersion);
        repository.saveFreeze(freeze, oldFreezeVersion);
        repository.appendLedger(
                account.id(), "UNFREEZE", command.quantity(),
                freeze.sourceSystem(), freeze.freezeNo());
        repository.appendOutbox(
                "StockUnfrozen", AGGREGATE_TYPE, freeze.id(), freeze.freezeNo(),
                freezePayload(freeze, account));
        repository.appendAudit(audit(
                command.operatorId(), "UNFREEZE_STOCK", command.reason(), freeze,
                before, accountSnapshot(account),
                command.requestId(), command.idempotencyKey()));
        repository.saveReceipt(
                command.idempotencyKey(), "UNFREEZE_STOCK", fingerprint, freeze.freezeNo());
        return view(freeze, false);
    }

    private FreezeResult duplicate(
            String idempotencyKey,
            String commandType,
            String requestFingerprint) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "幂等键不能为空");
        }
        InventoryWorkflowRepository.CommandReceipt receipt =
                repository.findReceipt(idempotencyKey);
        if (receipt == null) {
            return null;
        }
        if (!receipt.commandType().equals(commandType)
                || !receipt.requestFingerprint().equals(requestFingerprint)) {
            throw new BusinessException(
                    ErrorCode.IDEMPOTENCY_CONFLICT,
                    "同一幂等键对应的冻结请求内容不一致");
        }
        return view(requiredFreeze(receipt.aggregateNo()), true);
    }

    private InventoryAccountAggregate requiredAccount(
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo) {
        InventoryAccountAggregate account =
                repository.findAccount(ownerId, warehouseId, sku, batchNo);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        return account;
    }

    private StockFreezeAggregate requiredFreeze(String freezeNo) {
        StockFreezeAggregate freeze = repository.findFreeze(freezeNo);
        if (freeze == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "冻结单不存在");
        }
        return freeze;
    }

    private static void requireVersion(int actual, int expected, String message) {
        if (actual != expected) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, message);
        }
    }

    private static InventoryWorkflowRepository.AuditEntry audit(
            long operatorId,
            String operationType,
            String operationReason,
            StockFreezeAggregate freeze,
            String before,
            String after,
            String requestId,
            String idempotencyKey) {
        return new InventoryWorkflowRepository.AuditEntry(
                operatorId, operationType, operationReason, AGGREGATE_TYPE,
                freeze.id(), freeze.freezeNo(), before, after, requestId, idempotencyKey);
    }

    private static String accountSnapshot(InventoryAccountAggregate account) {
        return "{\"onHandQty\":\"" + account.onHandQty().toPlainString()
                + "\",\"availableQty\":\"" + account.availableQty().toPlainString()
                + "\",\"reservedQty\":\"" + account.reservedQty().toPlainString()
                + "\",\"frozenQty\":\"" + account.frozenQty().toPlainString()
                + "\",\"version\":" + account.version() + "}";
    }

    private static String freezePayload(
            StockFreezeAggregate freeze,
            InventoryAccountAggregate account) {
        return "{\"freezeNo\":\"" + json(freeze.freezeNo())
                + "\",\"reason\":\"" + json(freeze.reason())
                + "\",\"accountId\":" + account.id()
                + ",\"freezeQty\":\"" + freeze.freezeQty().toPlainString()
                + "\",\"unfrozenQty\":\"" + freeze.unfrozenQty().toPlainString()
                + "\",\"availableQty\":\"" + account.availableQty().toPlainString()
                + "\",\"frozenQty\":\"" + account.frozenQty().toPlainString()
                + "\",\"status\":" + freeze.status()
                + ",\"version\":" + freeze.version() + "}";
    }

    private static String fingerprint(Object... values) {
        return java.util.Arrays.stream(values)
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining("|"));
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static FreezeResult view(StockFreezeAggregate freeze, boolean idempotentHit) {
        return new FreezeResult(
                freeze.freezeNo(), freeze.status(), freeze.approvalStatus(),
                freeze.freezeQty(), freeze.unfrozenQty(), freeze.remainingFrozenQty(),
                freeze.version(), idempotentHit);
    }

    public record CreateFreezeCommand(
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal quantity,
            String reason,
            String sourceSystem,
            String sourceNo,
            boolean autoSubmit,
            long operatorId,
            String idempotencyKey,
            String requestId) {
    }

    public record ApproveFreezeCommand(
            String freezeNo,
            String result,
            String approvalNo,
            long operatorId,
            int version,
            String idempotencyKey,
            String requestId) {
    }

    public record UnfreezeCommand(
            String freezeNo,
            BigDecimal quantity,
            String reason,
            long operatorId,
            int version,
            String idempotencyKey,
            String requestId) {
    }

    public record FreezeResult(
            String freezeNo,
            int status,
            int approvalStatus,
            BigDecimal freezeQty,
            BigDecimal unfrozenQty,
            BigDecimal remainingFrozenQty,
            int version,
            boolean idempotentHit) {
    }

    public record InventoryScope(long ownerId, long warehouseId) {
    }
}
