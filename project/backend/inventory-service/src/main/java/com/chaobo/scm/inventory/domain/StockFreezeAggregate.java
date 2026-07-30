package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * 冻结单聚合根。
 *
 * <p>冻结单负责审批状态和冻结剩余量，库存账户只负责最终账本数量。把两者分开可以避免人工请求
 * 绕过审批直接减少可用库存，同时保留账户聚合对数量非负和乐观锁的保护。
 *
 * @author SCM Team
 */
public final class StockFreezeAggregate {

    public static final int STATUS_DRAFT = 1;
    public static final int STATUS_PENDING_APPROVAL = 2;
    public static final int STATUS_FROZEN = 3;
    public static final int STATUS_PARTIALLY_UNFROZEN = 4;
    public static final int STATUS_UNFROZEN = 5;
    public static final int STATUS_CANCELLED = 6;
    public static final int APPROVAL_DRAFT = 1;
    public static final int APPROVAL_PENDING = 2;
    public static final int APPROVAL_APPROVED = 3;
    public static final int APPROVAL_REJECTED = 4;

    private final long id;
    private final String freezeNo;
    private final long accountId;
    private final long ownerId;
    private final long warehouseId;
    private final String sku;
    private final String batchNo;
    private final BigDecimal freezeQty;
    private final String reason;
    private final String sourceSystem;
    private final String sourceNo;
    private final long createdBy;
    private BigDecimal unfrozenQty;
    private int status;
    private int approvalStatus;
    private String approvalNo;
    private Long approvedBy;
    private int version;

    private StockFreezeAggregate(
            long id,
            String freezeNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal freezeQty,
            BigDecimal unfrozenQty,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy,
            int status,
            int approvalStatus,
            String approvalNo,
            Long approvedBy,
            int version) {
        if (id <= 0 || accountId <= 0 || ownerId <= 0 || warehouseId <= 0) {
            throw validation("冻结单标识和库存维度不能为空");
        }
        if (blank(freezeNo) || blank(sku) || blank(reason) || blank(sourceSystem) || blank(sourceNo)) {
            throw validation("冻结单号、SKU、原因和来源不能为空");
        }
        requirePositive(freezeQty, "冻结数量必须大于0");
        this.id = id;
        this.freezeNo = freezeNo;
        this.accountId = accountId;
        this.ownerId = ownerId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.freezeQty = freezeQty;
        this.unfrozenQty = unfrozenQty == null ? BigDecimal.ZERO : unfrozenQty;
        this.reason = reason;
        this.sourceSystem = sourceSystem;
        this.sourceNo = sourceNo;
        this.createdBy = createdBy;
        this.status = status;
        this.approvalStatus = approvalStatus;
        this.approvalNo = approvalNo;
        this.approvedBy = approvedBy;
        this.version = version;
        if (this.unfrozenQty.signum() < 0 || this.unfrozenQty.compareTo(freezeQty) > 0) {
            throw validation("累计解冻数量非法");
        }
    }

    /**
     * 创建冻结草稿；创建本身不改变账户数量。
     */
    public static StockFreezeAggregate create(
            long id,
            String freezeNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal freezeQty,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy) {
        return new StockFreezeAggregate(
                id, freezeNo, accountId, ownerId, warehouseId, sku, batchNo,
                freezeQty, BigDecimal.ZERO, reason, sourceSystem, sourceNo,
                createdBy, STATUS_DRAFT, APPROVAL_DRAFT, null, null, 0);
    }

    /**
     * 从持久化状态恢复聚合，仍通过构造校验保护数量不变量。
     */
    public static StockFreezeAggregate restore(
            long id,
            String freezeNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal freezeQty,
            BigDecimal unfrozenQty,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy,
            int status,
            int approvalStatus,
            String approvalNo,
            Long approvedBy,
            int version) {
        return new StockFreezeAggregate(
                id, freezeNo, accountId, ownerId, warehouseId, sku, batchNo,
                freezeQty, unfrozenQty, reason, sourceSystem, sourceNo, createdBy,
                status, approvalStatus, approvalNo, approvedBy, version);
    }

    /**
     * 提交审批。只有草稿可以提交，防止重复提交制造多个审批实例。
     */
    public void submit() {
        requireStatus(STATUS_DRAFT, "只有草稿冻结单可以提交审批");
        status = STATUS_PENDING_APPROVAL;
        approvalStatus = APPROVAL_PENDING;
        version++;
    }

    /**
     * 审批通过并把单据推进到可落账的已冻结状态。
     *
     * <p>账户数量由应用服务在同一事务中变更；聚合只表达审批事实和执行资格。
     */
    public void approve(String newApprovalNo, long operatorId) {
        requireStatus(STATUS_PENDING_APPROVAL, "只有待审批冻结单可以审批");
        if (blank(newApprovalNo) || operatorId <= 0) {
            throw validation("审批单号和审批人不能为空");
        }
        requireDifferentApprover(operatorId);
        approvalNo = newApprovalNo;
        approvedBy = operatorId;
        approvalStatus = APPROVAL_APPROVED;
        status = STATUS_FROZEN;
        version++;
    }

    /**
     * 审批驳回后回到草稿，允许补齐证据后重新提交，但不改变库存账户。
     */
    public void reject(String newApprovalNo, long operatorId) {
        requireStatus(STATUS_PENDING_APPROVAL, "只有待审批冻结单可以驳回");
        if (blank(newApprovalNo) || operatorId <= 0) {
            throw validation("审批单号和审批人不能为空");
        }
        requireDifferentApprover(operatorId);
        approvalNo = newApprovalNo;
        approvedBy = operatorId;
        approvalStatus = APPROVAL_REJECTED;
        status = STATUS_DRAFT;
        version++;
    }

    /**
     * 登记一次解冻，允许部分解冻但累计数量不能超过原冻结数量。
     */
    public void unfreeze(BigDecimal quantity) {
        if (status != STATUS_FROZEN && status != STATUS_PARTIALLY_UNFROZEN) {
            throw state("当前冻结单不可解冻");
        }
        requirePositive(quantity, "解冻数量必须大于0");
        if (remainingFrozenQty().compareTo(quantity) < 0) {
            throw rule("解冻数量超过冻结剩余数量");
        }
        unfrozenQty = unfrozenQty.add(quantity);
        status = remainingFrozenQty().signum() == 0
                ? STATUS_UNFROZEN
                : STATUS_PARTIALLY_UNFROZEN;
        version++;
    }

    private void requireStatus(int expected, String message) {
        if (status != expected) {
            throw state(message);
        }
    }

    private void requireDifferentApprover(long operatorId) {
        if (createdBy == operatorId) {
            throw rule("申请人与审批人不能相同");
        }
    }

    private static void requirePositive(BigDecimal value, String message) {
        if (value == null || value.signum() <= 0) {
            throw validation(message);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static BusinessException validation(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    private static BusinessException state(String message) {
        return new BusinessException(ErrorCode.STATE_CONFLICT, message);
    }

    private static BusinessException rule(String message) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, message);
    }

    public long id() {
        return id;
    }

    public String freezeNo() {
        return freezeNo;
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

    public BigDecimal freezeQty() {
        return freezeQty;
    }

    public BigDecimal unfrozenQty() {
        return unfrozenQty;
    }

    public BigDecimal remainingFrozenQty() {
        return freezeQty.subtract(unfrozenQty);
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

    public int version() {
        return version;
    }
}
