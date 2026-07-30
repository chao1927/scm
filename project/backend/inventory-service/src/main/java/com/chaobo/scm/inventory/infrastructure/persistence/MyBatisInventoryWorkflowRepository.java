package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.application.InventoryWorkflowRepository;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.domain.InventoryAdjustmentAggregate;
import com.chaobo.scm.inventory.domain.StockFreezeAggregate;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * 冻结与调整工作流的 MyBatis 仓储实现。
 *
 * <p>仓储把领域对象转换为数据库行，并把更新行数为零统一解释为乐观锁冲突。
 *
 * @author SCM Team
 */
@Repository
public class MyBatisInventoryWorkflowRepository implements InventoryWorkflowRepository {

    private final InventoryWorkflowMapper workflow;
    private final InventoryMapper inventory;
    private final InventoryEventMapper events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public MyBatisInventoryWorkflowRepository(
            InventoryWorkflowMapper workflow,
            InventoryMapper inventory,
            InventoryEventMapper events) {
        this.workflow = workflow;
        this.inventory = inventory;
        this.events = events;
    }

    @Override
    public InventoryAccountAggregate findAccount(
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo) {
        return account(inventory.findAccount(ownerId, warehouseId, sku, batchNo));
    }

    @Override
    public InventoryAccountAggregate findAccountById(long accountId) {
        return account(inventory.findAccountById(accountId));
    }

    @Override
    public void saveAccount(InventoryAccountAggregate account, int expectedVersion) {
        int updated = inventory.updateAccount(
                account.id(), account.onHandQty(), account.availableQty(),
                account.reservedQty(), account.frozenQty(), account.version(), expectedVersion);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存账户版本冲突");
        }
    }

    @Override
    public StockFreezeAggregate findFreeze(String freezeNo) {
        InventoryWorkflowMapper.FreezeRow row = workflow.findFreeze(freezeNo);
        return row == null ? null : StockFreezeAggregate.restore(
                row.id(), row.freezeNo(), row.accountId(), row.ownerId(), row.warehouseId(),
                row.sku(), row.batchNo(), row.freezeQty(), row.unfrozenQty(), row.reason(),
                row.sourceSystem(), row.sourceNo(), row.createdBy(), row.status(),
                row.approvalStatus(), row.approvalNo(), row.approvedBy(), row.version());
    }

    @Override
    public void insertFreeze(StockFreezeAggregate freeze) {
        workflow.insertFreeze(freezeRow(freeze));
    }

    @Override
    public void saveFreeze(StockFreezeAggregate freeze, int expectedVersion) {
        if (workflow.updateFreeze(freezeRow(freeze), expectedVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "冻结单版本冲突");
        }
    }

    @Override
    public InventoryAdjustmentAggregate findAdjustment(String adjustmentNo) {
        InventoryWorkflowMapper.AdjustmentRow row =
                workflow.findAdjustment(adjustmentNo);
        return row == null ? null : InventoryAdjustmentAggregate.restore(
                row.id(), row.adjustmentNo(), row.accountId(), row.ownerId(), row.warehouseId(),
                row.sku(), row.batchNo(), row.adjustQty(), row.adjustmentType(), row.reason(),
                row.sourceSystem(), row.sourceNo(), row.createdBy(), row.status(),
                row.approvalStatus(), row.approvalNo(), row.approvedBy(), row.executedBy(),
                row.version());
    }

    @Override
    public void insertAdjustment(InventoryAdjustmentAggregate adjustment) {
        workflow.insertAdjustment(adjustmentRow(adjustment));
    }

    @Override
    public void saveAdjustment(
            InventoryAdjustmentAggregate adjustment,
            int expectedVersion) {
        if (workflow.updateAdjustment(adjustmentRow(adjustment), expectedVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存调整单版本冲突");
        }
    }

    @Override
    public CommandReceipt findReceipt(String idempotencyKey) {
        InventoryWorkflowMapper.ReceiptRow row = workflow.findReceipt(idempotencyKey);
        return row == null ? null : new CommandReceipt(
                row.idempotencyKey(), row.commandType(), row.requestFingerprint(),
                row.aggregateNo());
    }

    @Override
    public void saveReceipt(
            String idempotencyKey,
            String commandType,
            String requestFingerprint,
            String aggregateNo) {
        workflow.insertReceipt(
                idempotencyKey, commandType, requestFingerprint, aggregateNo);
    }

    @Override
    public void appendLedger(
            long accountId,
            String ledgerType,
            BigDecimal quantityDelta,
            String sourceSystem,
            String sourceNo) {
        long id = ids.incrementAndGet();
        inventory.insertLedger(
                id, "LED" + id, accountId, ledgerType, quantityDelta, sourceSystem, sourceNo);
    }

    @Override
    public void appendOutbox(
            String eventType,
            String aggregateType,
            long aggregateId,
            String aggregateNo,
            String payloadJson) {
        long id = ids.incrementAndGet();
        events.insertOutbox(
                id, "EVT" + id, eventType, aggregateType,
                String.valueOf(aggregateId), payloadJson);
    }

    @Override
    public void appendAudit(AuditEntry entry) {
        workflow.insertAudit(
                ids.incrementAndGet(), entry.operatorId(), entry.operationType(),
                entry.operationReason(), entry.targetType(), entry.targetId(), entry.targetNo(),
                entry.beforeSnapshot(), entry.afterSnapshot(), entry.requestId(),
                entry.idempotencyKey());
    }

    private static InventoryAccountAggregate account(InventoryMapper.AccountRow row) {
        return row == null ? null : new InventoryAccountAggregate(
                row.id(), row.ownerId(), row.warehouseId(), row.sku(), row.batchNo(),
                row.onHandQty(), row.availableQty(), row.reservedQty(), row.frozenQty(),
                row.version());
    }

    private static InventoryWorkflowMapper.FreezeRow freezeRow(
            StockFreezeAggregate freeze) {
        return new InventoryWorkflowMapper.FreezeRow(
                freeze.id(), freeze.freezeNo(), freeze.accountId(), freeze.ownerId(),
                freeze.warehouseId(), freeze.sku(), freeze.batchNo(), freeze.freezeQty(),
                freeze.unfrozenQty(), freeze.reason(), freeze.sourceSystem(), freeze.sourceNo(),
                freeze.createdBy(), freeze.status(), freeze.approvalStatus(), freeze.approvalNo(),
                freeze.approvedBy(), freeze.version());
    }

    private static InventoryWorkflowMapper.AdjustmentRow adjustmentRow(
            InventoryAdjustmentAggregate adjustment) {
        return new InventoryWorkflowMapper.AdjustmentRow(
                adjustment.id(), adjustment.adjustmentNo(), adjustment.accountId(),
                adjustment.ownerId(), adjustment.warehouseId(), adjustment.sku(),
                adjustment.batchNo(), adjustment.adjustQty(), adjustment.adjustmentType(),
                adjustment.reason(), adjustment.sourceSystem(), adjustment.sourceNo(),
                adjustment.createdBy(), adjustment.status(), adjustment.approvalStatus(),
                adjustment.approvalNo(), adjustment.approvedBy(), adjustment.executedBy(),
                adjustment.version());
    }
}
