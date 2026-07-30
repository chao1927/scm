package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.domain.InventoryAdjustmentAggregate;
import com.chaobo.scm.inventory.domain.StockFreezeAggregate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用服务测试用的状态型仓储替身。
 *
 * <p>该实现模拟真实仓储的乐观锁和幂等唯一键，测试观察最终业务状态，不依赖方法调用顺序。
 */
final class MemoryInventoryWorkflowRepository implements InventoryWorkflowRepository {

    final Map<Long, InventoryAccountAggregate> accounts = new HashMap<>();
    final Map<String, StockFreezeAggregate> freezes = new HashMap<>();
    final Map<String, InventoryAdjustmentAggregate> adjustments = new HashMap<>();
    final Map<String, CommandReceipt> receipts = new HashMap<>();
    final List<String> ledgers = new ArrayList<>();
    final List<String> events = new ArrayList<>();
    final List<AuditEntry> audits = new ArrayList<>();
    boolean forceAccountVersionConflict;

    void putAccount(InventoryAccountAggregate account) {
        accounts.put(account.id(), account);
    }

    @Override
    public InventoryAccountAggregate findAccount(
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo) {
        return accounts.values().stream()
                .filter(account -> account.ownerId() == ownerId
                        && account.warehouseId() == warehouseId
                        && account.sku().equals(sku))
                .findFirst()
                .orElse(null);
    }

    @Override
    public InventoryAccountAggregate findAccountById(long accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void saveAccount(InventoryAccountAggregate account, int expectedVersion) {
        if (forceAccountVersionConflict || account.version() != expectedVersion + 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存账户版本冲突");
        }
        accounts.put(account.id(), account);
    }

    @Override
    public StockFreezeAggregate findFreeze(String freezeNo) {
        return freezes.get(freezeNo);
    }

    @Override
    public void insertFreeze(StockFreezeAggregate freeze) {
        freezes.put(freeze.freezeNo(), freeze);
    }

    @Override
    public void saveFreeze(StockFreezeAggregate freeze, int expectedVersion) {
        if (freeze.version() != expectedVersion + 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "冻结单版本冲突");
        }
        freezes.put(freeze.freezeNo(), freeze);
    }

    @Override
    public InventoryAdjustmentAggregate findAdjustment(String adjustmentNo) {
        return adjustments.get(adjustmentNo);
    }

    @Override
    public void insertAdjustment(InventoryAdjustmentAggregate adjustment) {
        adjustments.put(adjustment.adjustmentNo(), adjustment);
    }

    @Override
    public void saveAdjustment(
            InventoryAdjustmentAggregate adjustment,
            int expectedVersion) {
        if (adjustment.version() != expectedVersion + 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "调整单版本冲突");
        }
        adjustments.put(adjustment.adjustmentNo(), adjustment);
    }

    @Override
    public CommandReceipt findReceipt(String idempotencyKey) {
        return receipts.get(idempotencyKey);
    }

    @Override
    public void saveReceipt(
            String idempotencyKey,
            String commandType,
            String requestFingerprint,
            String aggregateNo) {
        receipts.put(
                idempotencyKey,
                new CommandReceipt(
                        idempotencyKey,
                        commandType,
                        requestFingerprint,
                        aggregateNo));
    }

    @Override
    public void appendLedger(
            long accountId,
            String ledgerType,
            BigDecimal quantityDelta,
            String sourceSystem,
            String sourceNo) {
        ledgers.add(accountId + ":" + ledgerType + ":" + quantityDelta.toPlainString());
    }

    @Override
    public void appendOutbox(
            String eventType,
            String aggregateType,
            long aggregateId,
            String aggregateNo,
            String payloadJson) {
        events.add(eventType + ":" + aggregateNo);
    }

    @Override
    public void appendAudit(AuditEntry entry) {
        audits.add(entry);
    }
}
