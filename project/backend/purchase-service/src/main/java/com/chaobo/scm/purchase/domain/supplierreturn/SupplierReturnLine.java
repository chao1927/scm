package com.chaobo.scm.purchase.domain.supplierreturn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;

public class SupplierReturnLine {
    private final long lineId;
    private final String skuCode;
    private final BigDecimal returnQty;
    private final BigDecimal returnableQty;
    private final String reason;

    public SupplierReturnLine(long lineId, String skuCode, BigDecimal returnQty, BigDecimal returnableQty, String reason) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退供SKU不能为空");
        }
        if (returnQty == null || returnQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退供数量必须大于0");
        }
        if (returnableQty == null || returnQty.compareTo(returnableQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "退供数量不能超过可退数量");
        }
        this.lineId = lineId;
        this.skuCode = skuCode;
        this.returnQty = returnQty;
        this.returnableQty = returnableQty;
        this.reason = reason;
    }

    public long lineId() { return lineId; }
    public String skuCode() { return skuCode; }
    public BigDecimal returnQty() { return returnQty; }
    public BigDecimal returnableQty() { return returnableQty; }
    public String reason() { return reason; }
}
