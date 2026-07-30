package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * 库存调整单聚合根。
 *
 * <p>调整单固化调整方向、原因、来源和审批结果。它不直接持有账户余额，执行时由应用服务把已批准的
 * 调整量交给库存账户聚合，并在同一事务中保存账户、流水、事件和审计记录。
 *
 * @author SCM Team
 */
public final class InventoryAdjustmentAggregate {

    public static final int STATUS_DRAFT = 1;
    public static final int STATUS_PENDING_APPROVAL = 2;
    public static final int STATUS_APPROVED = 3;
    public static final int STATUS_EXECUTED = 4;
    public static final int STATUS_REJECTED = 5;
    public static final int STATUS_CANCELLED = 6;
    public static final int APPROVAL_DRAFT = 1;
    public static final int APPROVAL_PENDING = 2;
    public static final int APPROVAL_APPROVED = 3;
    public static final int APPROVAL_REJECTED = 4;

    private final long id;
    private final String adjustmentNo;
    private final long accountId;
    private final long ownerId;
    private final long warehouseId;
    private final String sku;
    private final String batchNo;
    private final BigDecimal adjustQty;
    private final String adjustmentType;
    private final String reason;
    private final String sourceSystem;
    private final String sourceNo;
    private final long createdBy;
    private int status;
    private int approvalStatus;
    private String approvalNo;
    private Long approvedBy;
    private Long executedBy;
    private int version;

    private InventoryAdjustmentAggregate(
            long id,
            String adjustmentNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal adjustQty,
            String adjustmentType,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy,
            int status,
            int approvalStatus,
            String approvalNo,
            Long approvedBy,
            Long executedBy,
            int version) {
        if (id <= 0 || accountId <= 0 || ownerId <= 0 || warehouseId <= 0) {
            throw validation("调整单标识和库存维度不能为空");
        }
        if (blank(adjustmentNo) || blank(sku) || blank(adjustmentType)
                || blank(reason) || blank(sourceSystem) || blank(sourceNo)) {
            throw validation("调整单号、SKU、类型、原因和来源不能为空");
        }
        if (adjustQty == null || adjustQty.signum() == 0) {
            throw validation("调整数量不能为0");
        }
        this.id = id;
        this.adjustmentNo = adjustmentNo;
        this.accountId = accountId;
        this.ownerId = ownerId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.adjustQty = adjustQty;
        this.adjustmentType = adjustmentType;
        this.reason = reason;
        this.sourceSystem = sourceSystem;
        this.sourceNo = sourceNo;
        this.createdBy = createdBy;
        this.status = status;
        this.approvalStatus = approvalStatus;
        this.approvalNo = approvalNo;
        this.approvedBy = approvedBy;
        this.executedBy = executedBy;
        this.version = version;
    }

    public static InventoryAdjustmentAggregate create(
            long id,
            String adjustmentNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal adjustQty,
            String adjustmentType,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy) {
        return new InventoryAdjustmentAggregate(
                id, adjustmentNo, accountId, ownerId, warehouseId, sku, batchNo,
                adjustQty, adjustmentType, reason, sourceSystem, sourceNo, createdBy,
                STATUS_DRAFT, APPROVAL_DRAFT, null, null, null, 0);
    }

    public static InventoryAdjustmentAggregate restore(
            long id,
            String adjustmentNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal adjustQty,
            String adjustmentType,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy,
            int status,
            int approvalStatus,
            String approvalNo,
            Long approvedBy,
            Long executedBy,
            int version) {
        return new InventoryAdjustmentAggregate(
                id, adjustmentNo, accountId, ownerId, warehouseId, sku, batchNo,
                adjustQty, adjustmentType, reason, sourceSystem, sourceNo, createdBy,
                status, approvalStatus, approvalNo, approvedBy, executedBy, version);
    }

    public void submit() {
        requireStatus(STATUS_DRAFT, "只有草稿调整单可以提交审批");
        status = STATUS_PENDING_APPROVAL;
        approvalStatus = APPROVAL_PENDING;
        version++;
    }

    public void approve(String newApprovalNo, long operatorId) {
        requireStatus(STATUS_PENDING_APPROVAL, "只有待审批调整单可以审批");
        requireApproval(newApprovalNo, operatorId);
        approvalNo = newApprovalNo;
        approvedBy = operatorId;
        approvalStatus = APPROVAL_APPROVED;
        status = STATUS_APPROVED;
        version++;
    }

    public void reject(String comment, long operatorId) {
        requireStatus(STATUS_PENDING_APPROVAL, "只有待审批调整单可以驳回");
        if (blank(comment) || operatorId <= 0) {
            throw validation("驳回原因和审批人不能为空");
        }
        requireDifferentApprover(operatorId);
        approvalNo = comment;
        approvedBy = operatorId;
        approvalStatus = APPROVAL_REJECTED;
        status = STATUS_REJECTED;
        version++;
    }

    public void execute(long operatorId) {
        requireStatus(STATUS_APPROVED, "调整单未审批通过，不能执行");
        if (operatorId <= 0) {
            throw validation("执行人不能为空");
        }
        executedBy = operatorId;
        status = STATUS_EXECUTED;
        version++;
    }

    private void requireStatus(int expected, String message) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, message);
        }
    }

    private void requireApproval(String value, long operatorId) {
        if (blank(value) || operatorId <= 0) {
            throw validation("审批单号和审批人不能为空");
        }
        requireDifferentApprover(operatorId);
    }

    private void requireDifferentApprover(long operatorId) {
        if (createdBy == operatorId) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_FAILED,
                    "申请人与审批人不能相同");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static BusinessException validation(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    public long id() {
        return id;
    }

    public String adjustmentNo() {
        return adjustmentNo;
    }

    public long accountId() {
        return accountId;
    }

    public long ownerId() {
        return ownerId;
    }

    public long warehouseId() {
        return warehouseId;
    }

    public String sku() {
        return sku;
    }

    public String batchNo() {
        return batchNo;
    }

    public BigDecimal adjustQty() {
        return adjustQty;
    }

    public String adjustmentType() {
        return adjustmentType;
    }

    public String reason() {
        return reason;
    }

    public String sourceSystem() {
        return sourceSystem;
    }

    public String sourceNo() {
        return sourceNo;
    }

    public long createdBy() {
        return createdBy;
    }

    public int status() {
        return status;
    }

    public int approvalStatus() {
        return approvalStatus;
    }

    public String approvalNo() {
        return approvalNo;
    }

    public Long approvedBy() {
        return approvedBy;
    }

    public Long executedBy() {
        return executedBy;
    }

    public int version() {
        return version;
    }
}
