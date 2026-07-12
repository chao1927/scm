package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import com.chaobo.scm.inventory.infrastructure.persistence.InventorySnapshotMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventorySnapshotApplicationServiceTest {
    private final AccountMemory accounts = new AccountMemory();
    private final SnapshotMemory snapshots = new SnapshotMemory();
    private final InventorySnapshotApplicationService service = new InventorySnapshotApplicationService(accounts, snapshots);

    @Test
    void generateSnapshotAndConfirmReconciliation() {
        accounts.accounts.add(new InventoryMapper.AccountRow(1, 1, 1, "SKU", null,
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, 0));

        var snapshot = service.generate(1);
        var reconcile = service.createReconcile(1, new BigDecimal("8"));
        var confirmed = service.confirm(reconcile.reconcileNo(), 0);

        assertThat(snapshot.onHandQty()).isEqualByComparingTo("10");
        assertThat(reconcile.differenceQty()).isEqualByComparingTo("-2");
        assertThat(confirmed.status()).isEqualTo(2);
    }

    private static class AccountMemory implements InventoryMapper {
        private final List<AccountRow> accounts = new ArrayList<>();
        public AccountRow findAccount(long ownerId, long warehouseId, String sku, String batchNo) { return null; }
        public AccountRow findAccountById(long id) { return accounts.stream().filter(row -> row.id() == id).findFirst().orElse(null); }
        public List<AccountRow> accounts(int limit) { return accounts; }
        public void insertAccount(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHand, BigDecimal available, BigDecimal reserved, BigDecimal frozen, int version) {}
        public int updateAccount(long id, BigDecimal onHand, BigDecimal available, BigDecimal reserved, BigDecimal frozen, int version, int oldVersion) { return 0; }
        public void insertLedger(long id, String no, long accountId, String type, BigDecimal qty, String sourceSystem, String sourceNo) {}
        public List<LedgerRow> ledgers(int limit) { return List.of(); }
        public ReservationRow findReservation(String reservationNo) { return null; }
        public ReservationRow findReservationBySource(String sourceSystem, String sourceNo) { return null; }
        public void insertReservation(long id, String no, long accountId, String sourceSystem, String sourceNo, BigDecimal reserved, BigDecimal released, int status, int version) {}
        public int updateReservation(long id, BigDecimal released, int status, int version, int oldVersion) { return 0; }
    }

    private static class SnapshotMemory implements InventorySnapshotMapper {
        private final List<SnapshotRow> snapshotRows = new ArrayList<>();
        private final List<ReconcileRow> reconcileRows = new ArrayList<>();
        public void insertSnapshot(long id, String no, long accountId, BigDecimal onHand, BigDecimal available) { snapshotRows.add(new SnapshotRow(id, no, accountId, onHand, available)); }
        public List<SnapshotRow> snapshots(int limit) { return snapshotRows; }
        public void insertReconcile(long id, String no, long accountId, BigDecimal systemQty, BigDecimal wmsQty, BigDecimal differenceQty) { reconcileRows.add(new ReconcileRow(id, no, accountId, systemQty, wmsQty, differenceQty, 1, 0)); }
        public ReconcileRow findReconcile(String no) { return reconcileRows.stream().filter(row -> row.reconcileNo().equals(no)).findFirst().orElse(null); }
        public List<ReconcileRow> reconciles(int limit) { return reconcileRows; }
        public int confirm(long id, int version) { var row = reconcileRows.stream().filter(value -> value.id() == id && value.version() == version).findFirst().orElse(null); if (row == null) return 0; reconcileRows.set(reconcileRows.indexOf(row), new ReconcileRow(row.id(), row.reconcileNo(), row.accountId(), row.systemQty(), row.wmsQty(), row.differenceQty(), 2, version + 1)); return 1; }
    }
}
