package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.domain.ReservationAggregate;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventoryApplicationService {
    private final InventoryMapper mapper;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public InventoryApplicationService(InventoryMapper mapper) {
        this.mapper = mapper;
    }

    public List<InventoryMapper.AccountRow> stocks(int limit) {
        return mapper.accounts(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    public List<InventoryMapper.LedgerRow> ledgers(int limit) {
        return mapper.ledgers(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    @Transactional
    public AccountResult inbound(AccountCommand command) {
        var account = loadOrCreate(command);
        int oldVersion = account.version();
        account.receive(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "INBOUND", command.qty(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    @Transactional
    public ReservationResult reserve(ReservationCommand command) {
        var existed = mapper.findReservationBySource(command.sourceSystem(), command.sourceNo());
        if (existed != null) {
            return reservationView(toReservation(existed), true);
        }
        var account = loadOrCreate(new AccountCommand(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo(), BigDecimal.ZERO, command.sourceSystem(), command.sourceNo()));
        int oldVersion = account.version();
        account.reserve(command.qty());
        save(account, oldVersion);
        var reservation = new ReservationAggregate(ids.incrementAndGet(), "RSV" + ids.incrementAndGet(), account.id(), command.sourceSystem(), command.sourceNo(), command.qty(), BigDecimal.ZERO, 1, 0);
        mapper.insertReservation(reservation.id(), reservation.reservationNo(), reservation.accountId(), reservation.sourceSystem(), reservation.sourceNo(), reservation.reservedQty(), reservation.releasedQty(), reservation.status(), reservation.version());
        ledger(account.id(), "RESERVE", command.qty().negate(), command.sourceSystem(), command.sourceNo());
        return reservationView(reservation, false);
    }

    @Transactional
    public ReservationResult release(String reservationNo) {
        var reservation = toReservation(requiredReservation(reservationNo));
        var account = toAccount(requiredAccount(reservation.accountId()));
        int oldAccountVersion = account.version();
        int oldReservationVersion = reservation.version();
        BigDecimal releaseQty = reservation.releaseAll();
        account.release(releaseQty);
        save(account, oldAccountVersion);
        if (mapper.updateReservation(reservation.id(), reservation.releasedQty(), reservation.status(), reservation.version(), oldReservationVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "预占版本冲突");
        }
        ledger(account.id(), "RELEASE", releaseQty, reservation.sourceSystem(), reservation.sourceNo());
        return reservationView(reservation, false);
    }

    @Transactional
    public AccountResult freeze(AccountCommand command) {
        var account = loadOrCreate(command);
        int oldVersion = account.version();
        account.freeze(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "FREEZE", command.qty().negate(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    @Transactional
    public AccountResult unfreeze(AccountCommand command) {
        var account = load(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        int oldVersion = account.version();
        account.unfreeze(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "UNFREEZE", command.qty(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    @Transactional
    public AccountResult adjust(AccountCommand command) {
        var account = loadOrCreate(command);
        int oldVersion = account.version();
        account.adjust(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "ADJUST", command.qty(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    @Transactional
    public AccountResult outbound(AccountCommand command) {
        var account = load(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        int oldVersion = account.version();
        account.outbound(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "OUTBOUND", command.qty().negate(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    private InventoryAccountAggregate loadOrCreate(AccountCommand command) {
        var row = mapper.findAccount(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        if (row != null) {
            return toAccount(row);
        }
        return new InventoryAccountAggregate(ids.incrementAndGet(), command.ownerId(), command.warehouseId(), command.sku(), command.batchNo(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }

    private InventoryAccountAggregate load(long ownerId, long warehouseId, String sku, String batchNo) {
        var row = mapper.findAccount(ownerId, warehouseId, sku, batchNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        return toAccount(row);
    }

    private InventoryMapper.AccountRow requiredAccount(long accountId) {
        var row = mapper.findAccountById(accountId);
        if (row == null) throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        return row;
    }

    private InventoryMapper.ReservationRow requiredReservation(String reservationNo) {
        var row = mapper.findReservation(reservationNo);
        if (row == null) throw new BusinessException(ErrorCode.NOT_FOUND, "预占单不存在");
        return row;
    }

    private void save(InventoryAccountAggregate account, int oldVersion) {
        if (mapper.findAccountById(account.id()) == null) {
            mapper.insertAccount(account.id(), account.ownerId(), account.warehouseId(), account.sku(), account.batchNo(), account.onHandQty(), account.availableQty(), account.reservedQty(), account.frozenQty(), account.version());
        } else if (mapper.updateAccount(account.id(), account.onHandQty(), account.availableQty(), account.reservedQty(), account.frozenQty(), account.version(), oldVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存账户版本冲突");
        }
    }

    private void ledger(long accountId, String type, BigDecimal qty, String sourceSystem, String sourceNo) {
        long id = ids.incrementAndGet();
        mapper.insertLedger(id, "LED" + id, accountId, type, qty, sourceSystem, sourceNo);
    }

    private static InventoryAccountAggregate toAccount(InventoryMapper.AccountRow row) {
        return new InventoryAccountAggregate(row.id(), row.ownerId(), row.warehouseId(), row.sku(), row.batchNo(), row.onHandQty(), row.availableQty(), row.reservedQty(), row.frozenQty(), row.version());
    }

    private static ReservationAggregate toReservation(InventoryMapper.ReservationRow row) {
        return new ReservationAggregate(row.id(), row.reservationNo(), row.accountId(), row.sourceSystem(), row.sourceNo(), row.reservedQty(), row.releasedQty(), row.status(), row.version());
    }

    private static AccountResult view(InventoryAccountAggregate account) {
        return new AccountResult(account.id(), account.ownerId(), account.warehouseId(), account.sku(), account.batchNo(), account.onHandQty(), account.availableQty(), account.reservedQty(), account.frozenQty(), account.version());
    }

    private static ReservationResult reservationView(ReservationAggregate reservation, boolean duplicated) {
        return new ReservationResult(reservation.reservationNo(), reservation.accountId(), reservation.reservedQty(), reservation.releasedQty(), reservation.status(), reservation.version(), duplicated);
    }

    public record AccountCommand(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String sourceSystem, String sourceNo) {}
    public record ReservationCommand(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String sourceSystem, String sourceNo) {}
    public record AccountResult(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHandQty, BigDecimal availableQty, BigDecimal reservedQty, BigDecimal frozenQty, int version) {}
    public record ReservationResult(String reservationNo, long accountId, BigDecimal reservedQty, BigDecimal releasedQty, int status, int version, boolean duplicated) {}
}
