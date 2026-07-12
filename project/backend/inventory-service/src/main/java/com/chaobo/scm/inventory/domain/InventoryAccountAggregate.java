package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class InventoryAccountAggregate {
    private final long id;
    private final long ownerId;
    private final long warehouseId;
    private final String sku;
    private final String batchNo;
    private BigDecimal onHandQty;
    private BigDecimal availableQty;
    private BigDecimal reservedQty;
    private BigDecimal frozenQty;
    private int version;

    public InventoryAccountAggregate(long id, long ownerId, long warehouseId, String sku, String batchNo,
                                     BigDecimal onHandQty, BigDecimal availableQty, BigDecimal reservedQty,
                                     BigDecimal frozenQty, int version) {
        if (ownerId <= 0 || warehouseId <= 0 || sku == null || sku.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "库存账户维度不能为空");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.onHandQty = zero(onHandQty);
        this.availableQty = zero(availableQty);
        this.reservedQty = zero(reservedQty);
        this.frozenQty = zero(frozenQty);
        this.version = version;
        ensureNonNegative();
    }

    public void receive(BigDecimal qty) {
        requirePositive(qty, "入库数量必须大于0");
        onHandQty = onHandQty.add(qty);
        availableQty = availableQty.add(qty);
        version++;
    }

    public void reserve(BigDecimal qty) {
        requirePositive(qty, "预占数量必须大于0");
        if (availableQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "可用库存不足");
        }
        availableQty = availableQty.subtract(qty);
        reservedQty = reservedQty.add(qty);
        version++;
    }

    public void release(BigDecimal qty) {
        requirePositive(qty, "释放数量必须大于0");
        if (reservedQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "释放数量超过预占数量");
        }
        reservedQty = reservedQty.subtract(qty);
        availableQty = availableQty.add(qty);
        version++;
    }

    public void freeze(BigDecimal qty) {
        requirePositive(qty, "冻结数量必须大于0");
        if (availableQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "可冻结库存不足");
        }
        availableQty = availableQty.subtract(qty);
        frozenQty = frozenQty.add(qty);
        version++;
    }

    public void unfreeze(BigDecimal qty) {
        requirePositive(qty, "解冻数量必须大于0");
        if (frozenQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "解冻数量超过冻结数量");
        }
        frozenQty = frozenQty.subtract(qty);
        availableQty = availableQty.add(qty);
        version++;
    }

    public void adjust(BigDecimal qtyDelta) {
        if (qtyDelta == null || qtyDelta.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "调整数量不能为0");
        }
        onHandQty = onHandQty.add(qtyDelta);
        availableQty = availableQty.add(qtyDelta);
        ensureNonNegative();
        version++;
    }

    public void outbound(BigDecimal qty) {
        requirePositive(qty, "出库扣减数量必须大于0");
        if (reservedQty.compareTo(qty) >= 0) {
            reservedQty = reservedQty.subtract(qty);
        } else if (availableQty.compareTo(qty) >= 0) {
            availableQty = availableQty.subtract(qty);
        } else {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "库存扣减数量不足");
        }
        onHandQty = onHandQty.subtract(qty);
        ensureNonNegative();
        version++;
    }

    private void ensureNonNegative() {
        if (onHandQty.signum() < 0 || availableQty.signum() < 0 || reservedQty.signum() < 0 || frozenQty.signum() < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "库存账户数量不能小于0");
        }
    }

    private static void requirePositive(BigDecimal value, String message) {
        if (value == null || value.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, message);
        }
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public long id() { return id; }
    public long ownerId() { return ownerId; }
    public long warehouseId() { return warehouseId; }
    public String sku() { return sku; }
    public String batchNo() { return batchNo; }
    public BigDecimal onHandQty() { return onHandQty; }
    public BigDecimal availableQty() { return availableQty; }
    public BigDecimal reservedQty() { return reservedQty; }
    public BigDecimal frozenQty() { return frozenQty; }
    public int version() { return version; }
}
