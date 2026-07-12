package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryApplicationServiceTest {
    private final MemoryInventoryMapper mapper = new MemoryInventoryMapper();
    private final InventoryApplicationService service = new InventoryApplicationService(mapper);

    @Test
    void inboundReserveReleaseFreezeAndAdjustWritesLedger() {
        service.inbound(cmd(BigDecimal.TEN, "PUTAWAY-1"));
        var reservation = service.reserve(new InventoryApplicationService.ReservationCommand(
                1, 1, "SKU", null, new BigDecimal("4"), "OMS", "SO-1"
        ));
        service.release(reservation.reservationNo());
        service.freeze(cmd(new BigDecimal("2"), "FRZ-1"));
        service.unfreeze(cmd(BigDecimal.ONE, "FRZ-1"));
        service.adjust(cmd(BigDecimal.ONE, "ADJ-1"));

        var account = mapper.accounts.get(0);
        assertThat(account.onHandQty()).isEqualByComparingTo("11");
        assertThat(account.availableQty()).isEqualByComparingTo("10");
        assertThat(account.frozenQty()).isEqualByComparingTo("1");
        assertThat(mapper.ledgers).hasSize(6);
    }

    @Test
    void reservationIsIdempotentBySource() {
        service.inbound(cmd(BigDecimal.TEN, "PUTAWAY-2"));
        service.reserve(new InventoryApplicationService.ReservationCommand(1, 1, "SKU", null, BigDecimal.ONE, "OMS", "SO-2"));

        var duplicated = service.reserve(new InventoryApplicationService.ReservationCommand(1, 1, "SKU", null, BigDecimal.ONE, "OMS", "SO-2"));

        assertThat(duplicated.duplicated()).isTrue();
        assertThat(mapper.reservations).hasSize(1);
    }

    private static InventoryApplicationService.AccountCommand cmd(BigDecimal qty, String sourceNo) {
        return new InventoryApplicationService.AccountCommand(1, 1, "SKU", null, qty, "WMS", sourceNo);
    }

    private static class MemoryInventoryMapper implements InventoryMapper {
        private final List<AccountRow> accounts = new ArrayList<>();
        private final List<LedgerRow> ledgers = new ArrayList<>();
        private final List<ReservationRow> reservations = new ArrayList<>();

        public AccountRow findAccount(long ownerId, long warehouseId, String sku, String batchNo) {
            return accounts.stream().filter(row -> row.ownerId() == ownerId && row.warehouseId() == warehouseId && row.sku().equals(sku)).findFirst().orElse(null);
        }
        public AccountRow findAccountById(long id) { return accounts.stream().filter(row -> row.id() == id).findFirst().orElse(null); }
        public List<AccountRow> accounts(int limit) { return accounts.stream().limit(limit).toList(); }
        public void insertAccount(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHand, BigDecimal available, BigDecimal reserved, BigDecimal frozen, int version) { accounts.add(new AccountRow(id, ownerId, warehouseId, sku, batchNo, onHand, available, reserved, frozen, version)); }
        public int updateAccount(long id, BigDecimal onHand, BigDecimal available, BigDecimal reserved, BigDecimal frozen, int version, int oldVersion) {
            var row = findAccountById(id);
            if (row == null || row.version() != oldVersion) return 0;
            accounts.set(accounts.indexOf(row), new AccountRow(id, row.ownerId(), row.warehouseId(), row.sku(), row.batchNo(), onHand, available, reserved, frozen, version));
            return 1;
        }
        public void insertLedger(long id, String no, long accountId, String type, BigDecimal qty, String sourceSystem, String sourceNo) { ledgers.add(new LedgerRow(id, no, accountId, type, qty, sourceSystem, sourceNo)); }
        public List<LedgerRow> ledgers(int limit) { return ledgers.stream().limit(limit).toList(); }
        public ReservationRow findReservation(String reservationNo) { return reservations.stream().filter(row -> row.reservationNo().equals(reservationNo)).findFirst().orElse(null); }
        public ReservationRow findReservationBySource(String sourceSystem, String sourceNo) { return reservations.stream().filter(row -> row.sourceSystem().equals(sourceSystem) && row.sourceNo().equals(sourceNo)).findFirst().orElse(null); }
        public void insertReservation(long id, String no, long accountId, String sourceSystem, String sourceNo, BigDecimal reserved, BigDecimal released, int status, int version) { reservations.add(new ReservationRow(id, no, accountId, sourceSystem, sourceNo, reserved, released, status, version)); }
        public int updateReservation(long id, BigDecimal released, int status, int version, int oldVersion) {
            var row = reservations.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) return 0;
            reservations.set(reservations.indexOf(row), new ReservationRow(id, row.reservationNo(), row.accountId(), row.sourceSystem(), row.sourceNo(), row.reservedQty(), released, status, version));
            return 1;
        }
    }
}
