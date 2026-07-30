package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.domain.InventoryAdjustmentAggregate;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存调整单应用服务。
 *
 * <p>创建、提交和审批只推进调整单；只有已批准调整单执行时才允许修改库存账户并追加调整流水。
 *
 * @author SCM Team
 */
@Service
public class InventoryAdjustmentApplicationService {

    private static final String AGGREGATE_TYPE = "InventoryAdjustment";
    private static final String APPROVAL_RESULT_APPROVE = "APPROVE";
    private static final String APPROVAL_RESULT_REJECT = "REJECT";
    private final InventoryWorkflowRepository repository;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public InventoryAdjustmentApplicationService(InventoryWorkflowRepository repository) {
        this.repository = repository;
    }

    /**
     * 返回调整单所属数据范围，接口层必须在状态变更前完成权限校验。
     */
    public InventoryScope scope(String adjustmentNo) {
        InventoryAdjustmentAggregate adjustment = requiredAdjustment(adjustmentNo);
        return new InventoryScope(adjustment.ownerId(), adjustment.warehouseId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AdjustmentResult create(CreateAdjustmentCommand command) {
        String fingerprint = fingerprint(
                "CREATE", command.ownerId(), command.warehouseId(), command.sku(),
                command.batchNo(), command.quantity(), command.adjustmentType(),
                command.reason(), command.sourceSystem(), command.sourceNo(), command.autoSubmit());
        AdjustmentResult duplicate =
                duplicate(command.idempotencyKey(), "CREATE_ADJUSTMENT", fingerprint);
        if (duplicate != null) {
            return duplicate;
        }
        InventoryAccountAggregate account = requiredAccount(
                command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        long id = ids.incrementAndGet();
        InventoryAdjustmentAggregate adjustment = InventoryAdjustmentAggregate.create(
                id, "ADJ" + id, account.id(), command.ownerId(), command.warehouseId(),
                command.sku(), command.batchNo(), command.quantity(), command.adjustmentType(),
                command.reason(), command.sourceSystem(), command.sourceNo(), command.operatorId());
        if (command.autoSubmit()) {
            adjustment.submit();
        }
        repository.insertAdjustment(adjustment);
        repository.appendOutbox(
                "StockAdjustmentCreated", AGGREGATE_TYPE, adjustment.id(),
                adjustment.adjustmentNo(), payload(adjustment, account));
        repository.appendAudit(audit(
                command.operatorId(), "CREATE_ADJUSTMENT", command.reason(), adjustment, "{}",
                payload(adjustment, account), command.requestId(), command.idempotencyKey()));
        repository.saveReceipt(
                command.idempotencyKey(), "CREATE_ADJUSTMENT", fingerprint,
                adjustment.adjustmentNo());
        return view(adjustment, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdjustmentResult submit(SubmitAdjustmentCommand command) {
        String fingerprint = fingerprint(
                "SUBMIT", command.adjustmentNo(), command.operatorId(), command.version());
        AdjustmentResult duplicate =
                duplicate(command.idempotencyKey(), "SUBMIT_ADJUSTMENT", fingerprint);
        if (duplicate != null) {
            return duplicate;
        }
        InventoryAdjustmentAggregate adjustment = requiredAdjustment(command.adjustmentNo());
        requireVersion(adjustment.version(), command.version());
        int oldVersion = adjustment.version();
        adjustment.submit();
        repository.saveAdjustment(adjustment, oldVersion);
        repository.appendOutbox(
                "StockAdjustmentSubmitted", AGGREGATE_TYPE, adjustment.id(),
                adjustment.adjustmentNo(), payload(adjustment, null));
        repository.appendAudit(audit(
                command.operatorId(), "SUBMIT_ADJUSTMENT", "提交审批", adjustment, "{}",
                payload(adjustment, null), command.requestId(), command.idempotencyKey()));
        repository.saveReceipt(
                command.idempotencyKey(), "SUBMIT_ADJUSTMENT", fingerprint,
                adjustment.adjustmentNo());
        return view(adjustment, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdjustmentResult approve(ApproveAdjustmentCommand command) {
        String fingerprint = fingerprint(
                "APPROVE", command.adjustmentNo(), command.result(), command.approvalNo(),
                command.operatorId(), command.version());
        AdjustmentResult duplicate =
                duplicate(command.idempotencyKey(), "APPROVE_ADJUSTMENT", fingerprint);
        if (duplicate != null) {
            return duplicate;
        }
        InventoryAdjustmentAggregate adjustment = requiredAdjustment(command.adjustmentNo());
        requireVersion(adjustment.version(), command.version());
        int oldVersion = adjustment.version();
        String eventType;
        if (APPROVAL_RESULT_APPROVE.equalsIgnoreCase(command.result())) {
            adjustment.approve(command.approvalNo(), command.operatorId());
            eventType = "StockAdjustmentApproved";
        } else if (APPROVAL_RESULT_REJECT.equalsIgnoreCase(command.result())) {
            adjustment.reject(command.approvalNo(), command.operatorId());
            eventType = "StockAdjustmentRejected";
        } else {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "审批结果只允许 APPROVE 或 REJECT");
        }
        repository.saveAdjustment(adjustment, oldVersion);
        repository.appendOutbox(
                eventType, AGGREGATE_TYPE, adjustment.id(), adjustment.adjustmentNo(),
                payload(adjustment, null));
        repository.appendAudit(audit(
                command.operatorId(), "APPROVE_ADJUSTMENT",
                command.result() + ":" + command.approvalNo(), adjustment, "{}",
                payload(adjustment, null), command.requestId(), command.idempotencyKey()));
        repository.saveReceipt(
                command.idempotencyKey(), "APPROVE_ADJUSTMENT", fingerprint,
                adjustment.adjustmentNo());
        return view(adjustment, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdjustmentResult execute(ExecuteAdjustmentCommand command) {
        String fingerprint = fingerprint(
                "EXECUTE", command.adjustmentNo(), command.remark(),
                command.operatorId(), command.version());
        AdjustmentResult duplicate =
                duplicate(command.idempotencyKey(), "EXECUTE_ADJUSTMENT", fingerprint);
        if (duplicate != null) {
            return duplicate;
        }
        InventoryAdjustmentAggregate adjustment = requiredAdjustment(command.adjustmentNo());
        requireVersion(adjustment.version(), command.version());
        InventoryAccountAggregate account =
                repository.findAccountById(adjustment.accountId());
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        int oldAdjustmentVersion = adjustment.version();
        int oldAccountVersion = account.version();
        String before = accountSnapshot(account);
        adjustment.execute(command.operatorId());
        account.adjust(adjustment.adjustQty());
        repository.saveAccount(account, oldAccountVersion);
        repository.saveAdjustment(adjustment, oldAdjustmentVersion);
        repository.appendLedger(
                account.id(), "ADJUST", adjustment.adjustQty(),
                adjustment.sourceSystem(), adjustment.adjustmentNo());
        repository.appendOutbox(
                "StockAdjusted", AGGREGATE_TYPE, adjustment.id(),
                adjustment.adjustmentNo(), payload(adjustment, account));
        repository.appendAudit(audit(
                command.operatorId(), "EXECUTE_ADJUSTMENT", command.remark(), adjustment, before,
                accountSnapshot(account), command.requestId(), command.idempotencyKey()));
        repository.saveReceipt(
                command.idempotencyKey(), "EXECUTE_ADJUSTMENT", fingerprint,
                adjustment.adjustmentNo());
        return view(adjustment, false);
    }

    private AdjustmentResult duplicate(
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
                    "同一幂等键对应的调整请求内容不一致");
        }
        return view(requiredAdjustment(receipt.aggregateNo()), true);
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

    private InventoryAdjustmentAggregate requiredAdjustment(String adjustmentNo) {
        InventoryAdjustmentAggregate adjustment = repository.findAdjustment(adjustmentNo);
        if (adjustment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存调整单不存在");
        }
        return adjustment;
    }

    private static void requireVersion(int actual, int expected) {
        if (actual != expected) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存调整单版本冲突");
        }
    }

    private static InventoryWorkflowRepository.AuditEntry audit(
            long operatorId,
            String operationType,
            String operationReason,
            InventoryAdjustmentAggregate adjustment,
            String before,
            String after,
            String requestId,
            String idempotencyKey) {
        return new InventoryWorkflowRepository.AuditEntry(
                operatorId, operationType, operationReason, AGGREGATE_TYPE, adjustment.id(),
                adjustment.adjustmentNo(), before, after, requestId, idempotencyKey);
    }

    private static String accountSnapshot(InventoryAccountAggregate account) {
        return "{\"onHandQty\":\"" + account.onHandQty().toPlainString()
                + "\",\"availableQty\":\"" + account.availableQty().toPlainString()
                + "\",\"frozenQty\":\"" + account.frozenQty().toPlainString()
                + "\",\"version\":" + account.version() + "}";
    }

    private static String payload(
            InventoryAdjustmentAggregate adjustment,
            InventoryAccountAggregate account) {
        String quantities = account == null
                ? ""
                : ",\"onHandQty\":\"" + account.onHandQty().toPlainString()
                        + "\",\"availableQty\":\""
                        + account.availableQty().toPlainString() + "\"";
        return "{\"adjustmentNo\":\"" + json(adjustment.adjustmentNo())
                + "\",\"adjustmentType\":\"" + json(adjustment.adjustmentType())
                + "\",\"reason\":\"" + json(adjustment.reason())
                + "\",\"adjustQty\":\"" + adjustment.adjustQty().toPlainString()
                + "\",\"status\":" + adjustment.status()
                + ",\"version\":" + adjustment.version()
                + quantities + "}";
    }

    private static String fingerprint(Object... values) {
        return java.util.Arrays.stream(values)
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining("|"));
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static AdjustmentResult view(
            InventoryAdjustmentAggregate adjustment,
            boolean idempotentHit) {
        return new AdjustmentResult(
                adjustment.adjustmentNo(), adjustment.status(),
                adjustment.approvalStatus(), adjustment.adjustQty(),
                adjustment.version(), idempotentHit);
    }

    public record CreateAdjustmentCommand(
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal quantity,
            String adjustmentType,
            String reason,
            String sourceSystem,
            String sourceNo,
            boolean autoSubmit,
            long operatorId,
            String idempotencyKey,
            String requestId) {
    }

    public record SubmitAdjustmentCommand(
            String adjustmentNo,
            long operatorId,
            int version,
            String idempotencyKey,
            String requestId) {
    }

    public record ApproveAdjustmentCommand(
            String adjustmentNo,
            String result,
            String approvalNo,
            long operatorId,
            int version,
            String idempotencyKey,
            String requestId) {
    }

    public record ExecuteAdjustmentCommand(
            String adjustmentNo,
            String remark,
            long operatorId,
            int version,
            String idempotencyKey,
            String requestId) {
    }

    public record AdjustmentResult(
            String adjustmentNo,
            int status,
            int approvalStatus,
            BigDecimal adjustQty,
            int version,
            boolean idempotentHit) {
    }

    public record InventoryScope(long ownerId, long warehouseId) {
    }
}
