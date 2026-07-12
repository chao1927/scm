package com.chaobo.scm.wms.domain.operation;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class StocktakeAggregate {
    private final long id;
    private final String stocktakeNo;
    private final long warehouseId;
    private final String sku;
    private final BigDecimal differenceQty;
    private int status;
    private int version;

    public StocktakeAggregate(long id, String stocktakeNo, long warehouseId, String sku, BigDecimal differenceQty, int status, int version) {
        if (stocktakeNo == null || stocktakeNo.isBlank() || warehouseId <= 0 || sku == null || sku.isBlank()
                || differenceQty == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "盘点差异数据不合法");
        }
        this.id = id;
        this.stocktakeNo = stocktakeNo;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.differenceQty = differenceQty;
        this.status = status;
        this.version = version;
    }

    public void confirmDifference() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "盘点差异当前不可确认");
        }
        status = 2;
        version++;
    }

    public long id() { return id; }
    public String stocktakeNo() { return stocktakeNo; }
    public long warehouseId() { return warehouseId; }
    public String sku() { return sku; }
    public BigDecimal differenceQty() { return differenceQty; }
    public int status() { return status; }
    public int version() { return version; }
}
