package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import com.chaobo.scm.inventory.infrastructure.persistence.InventorySnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventorySnapshotApplicationService {
    private final InventoryMapper inventory;
    private final InventorySnapshotMapper snapshots;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public InventorySnapshotApplicationService(InventoryMapper inventory, InventorySnapshotMapper snapshots) {
        this.inventory = inventory;
        this.snapshots = snapshots;
    }

    @Transactional
    public SnapshotResult generate(long accountId) {
        var account = inventory.findAccountById(accountId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        long id = ids.incrementAndGet();
        String no = "SNP" + id;
        snapshots.insertSnapshot(id, no, account.id(), account.onHandQty(), account.availableQty());
        return new SnapshotResult(no, account.id(), account.onHandQty(), account.availableQty());
    }

    @Transactional
    public ReconcileResult createReconcile(long accountId, BigDecimal wmsQty) {
        var account = inventory.findAccountById(accountId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        long id = ids.incrementAndGet();
        BigDecimal difference = wmsQty.subtract(account.onHandQty());
        String no = "REC" + id;
        snapshots.insertReconcile(id, no, account.id(), account.onHandQty(), wmsQty, difference);
        return new ReconcileResult(no, account.id(), account.onHandQty(), wmsQty, difference, 1, 0);
    }

    @Transactional
    public ReconcileResult confirm(String reconcileNo, int version) {
        var row = snapshots.findReconcile(reconcileNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存对账单不存在");
        }
        if (snapshots.confirm(row.id(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存对账单版本冲突");
        }
        return new ReconcileResult(row.reconcileNo(), row.accountId(), row.systemQty(), row.wmsQty(), row.differenceQty(), 2, version + 1);
    }

    public List<InventorySnapshotMapper.SnapshotRow> snapshots(int limit) {
        return snapshots.snapshots(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    public List<InventorySnapshotMapper.ReconcileRow> reconciles(int limit) {
        return snapshots.reconciles(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    public record SnapshotResult(String snapshotNo, long accountId, BigDecimal onHandQty, BigDecimal availableQty) {}
    public record ReconcileResult(String reconcileNo, long accountId, BigDecimal systemQty, BigDecimal wmsQty, BigDecimal differenceQty, int status, int version) {}
}
