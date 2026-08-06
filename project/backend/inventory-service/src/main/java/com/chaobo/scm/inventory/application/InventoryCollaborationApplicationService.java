package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.integration.InventoryCollaborationApi;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryCollaborationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/** 将退供同步锁定命令落到真实冻结聚合、库存流水与 Outbox。 */
@Service
public class InventoryCollaborationApplicationService {

    private static final String LOCK = "LOCK_SUPPLIER_RETURN";
    private static final String RELEASE = "RELEASE_SUPPLIER_RETURN";
    private final StockFreezeApplicationService freezes;
    private final InventoryCollaborationMapper mapper;

    public InventoryCollaborationApplicationService(StockFreezeApplicationService freezes,
                                                    InventoryCollaborationMapper mapper) {
        this.freezes = freezes;
        this.mapper = mapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public InventoryCollaborationApi.LockResult lock(InventoryCollaborationApi.ReturnLockCommand command) {
        requireKey(command.idempotencyKey());
        if (command.returnId() <= 0 || command.returnNo() == null || command.returnNo().isBlank()
                || command.supplierId() <= 0 || command.warehouseId() <= 0
                || command.lines() == null || command.lines().isEmpty()) {
            throw invalid("退供库存锁定数据不完整");
        }
        String fingerprint = command.toString();
        InventoryCollaborationMapper.Receipt duplicate = receipt(command.idempotencyKey(), LOCK,
                fingerprint);
        if (duplicate != null) {
            return view(mapper.findLock(command.returnId()));
        }
        InventoryCollaborationMapper.Lock existed = mapper.findLock(command.returnId());
        if (existed != null) {
            if (!existed.requestFingerprint().equals(fingerprint)) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "同一退供单已存在不同的库存锁定快照");
            }
            mapper.insertReceipt(new InventoryCollaborationMapper.Receipt(command.idempotencyKey(), LOCK,
                    fingerprint, existed.lockNo()));
            return view(existed);
        }
        String lockNo = "SRL" + command.returnId();
        mapper.insertLock(new InventoryCollaborationMapper.Lock(command.returnId(), lockNo,
                command.returnNo(), command.supplierId(), command.warehouseId(), fingerprint, 1, 0));
        for (InventoryCollaborationApi.Line line : command.lines()) {
            var created = freezes.create(new StockFreezeApplicationService.CreateFreezeCommand(
                    command.supplierId(), command.warehouseId(), line.skuCode(), line.batchNo(),
                    line.quantity(), "供应商退供锁定", "SUPPLIER", command.returnNo(), true,
                    0, command.idempotencyKey() + "-CREATE-" + line.lineId(),
                    command.idempotencyKey()));
            var approved = freezes.approve(new StockFreezeApplicationService.ApproveFreezeCommand(
                    created.freezeNo(), "APPROVE", "RPC-" + lockNo, 0, created.version(),
                    command.idempotencyKey() + "-APPROVE-" + line.lineId(), command.idempotencyKey()));
            mapper.insertLine(new InventoryCollaborationMapper.LockLine(command.returnId(), line.lineId(),
                    approved.freezeNo(), line.quantity(), approved.version()));
        }
        mapper.insertReceipt(new InventoryCollaborationMapper.Receipt(command.idempotencyKey(), LOCK,
                fingerprint, lockNo));
        return view(mapper.findLock(command.returnId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void release(InventoryCollaborationApi.ReturnReleaseCommand command) {
        requireKey(command.idempotencyKey());
        if (command.returnId() <= 0 || command.lockNo() == null || command.lockNo().isBlank()
                || command.reason() == null || command.reason().isBlank()) {
            throw invalid("退供库存释放数据不完整");
        }
        String fingerprint = command.toString();
        if (receipt(command.idempotencyKey(), RELEASE, fingerprint) != null) {
            return;
        }
        InventoryCollaborationMapper.Lock lock = mapper.findLock(command.returnId());
        if (lock == null || !lock.lockNo().equals(command.lockNo())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退供库存锁定不存在");
        }
        if (lock.status() == 1) {
            for (InventoryCollaborationMapper.LockLine line : mapper.lockLines(command.returnId())) {
                freezes.unfreeze(new StockFreezeApplicationService.UnfreezeCommand(line.freezeNo(),
                        line.lockedQuantity(), command.reason(), 0, line.freezeVersion(),
                        command.idempotencyKey() + "-" + line.sourceLineId(), command.idempotencyKey()));
            }
            if (mapper.release(command.returnId()) != 1) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "退供库存锁定状态已变更");
            }
        }
        mapper.insertReceipt(new InventoryCollaborationMapper.Receipt(command.idempotencyKey(), RELEASE,
                fingerprint, lock.lockNo()));
    }

    private InventoryCollaborationApi.LockResult view(InventoryCollaborationMapper.Lock lock) {
        if (lock == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退供库存锁定不存在");
        }
        var lines = new ArrayList<InventoryCollaborationApi.LockedLine>();
        mapper.lockLines(lock.returnId()).forEach(line -> lines.add(
                new InventoryCollaborationApi.LockedLine(line.sourceLineId(), line.lockedQuantity())));
        return new InventoryCollaborationApi.LockResult(lock.status() == 1, lock.lockNo(), lines,
                lock.status() == 1 ? null : "锁定已释放");
    }

    private InventoryCollaborationMapper.Receipt receipt(String key, String type, String fingerprint) {
        InventoryCollaborationMapper.Receipt receipt = mapper.findReceipt(key);
        if (receipt != null && (!receipt.commandType().equals(type)
                || !receipt.requestFingerprint().equals(fingerprint))) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "Dubbo 幂等键已用于不同库存命令");
        }
        return receipt;
    }

    private static void requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw invalid("Dubbo 命令必须提供幂等键");
        }
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
