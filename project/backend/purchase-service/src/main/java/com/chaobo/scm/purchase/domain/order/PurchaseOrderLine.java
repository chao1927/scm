package com.chaobo.scm.purchase.domain.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PurchaseOrderLine {
    private final long lineId;
    private final String skuCode;
    private final String skuName;
    private BigDecimal orderQty;
    private final BigDecimal unitPrice;
    private final BigDecimal taxRate;
    private final BigDecimal taxIncludedPrice;
    private final LocalDate requiredDeliveryDate;
    private BigDecimal receivedQty;

    public PurchaseOrderLine(
            long lineId,
            String skuCode,
            String skuName,
            BigDecimal orderQty,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal taxIncludedPrice,
            LocalDate requiredDeliveryDate,
            BigDecimal receivedQty) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单SKU不能为空");
        }
        if (orderQty == null || orderQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购数量必须大于0");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购单价不能小于0");
        }
        this.lineId = lineId;
        this.skuCode = skuCode;
        this.skuName = skuName;
        this.orderQty = orderQty;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate == null ? BigDecimal.ZERO : taxRate;
        this.taxIncludedPrice = taxIncludedPrice == null ? taxIncluded(unitPrice, this.taxRate) : taxIncludedPrice;
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.receivedQty = receivedQty == null ? BigDecimal.ZERO : receivedQty;
    }

    public void changeQty(BigDecimal newQty) {
        if (newQty == null || newQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "变更后采购数量必须大于0");
        }
        if (newQty.compareTo(receivedQty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "采购数量不能小于已收货数量");
        }
        this.orderQty = newQty;
    }

    public BigDecimal amount() {
        return orderQty.multiply(unitPrice).setScale(6, RoundingMode.HALF_UP);
    }

    public BigDecimal taxAmount() {
        return amount().multiply(taxRate).setScale(6, RoundingMode.HALF_UP);
    }

    private static BigDecimal taxIncluded(BigDecimal unitPrice, BigDecimal taxRate) {
        return unitPrice.multiply(BigDecimal.ONE.add(taxRate)).setScale(6, RoundingMode.HALF_UP);
    }

    public long lineId() { return lineId; }
    public String skuCode() { return skuCode; }
    public String skuName() { return skuName; }
    public BigDecimal orderQty() { return orderQty; }
    public BigDecimal unitPrice() { return unitPrice; }
    public BigDecimal taxRate() { return taxRate; }
    public BigDecimal taxIncludedPrice() { return taxIncludedPrice; }
    public LocalDate requiredDeliveryDate() { return requiredDeliveryDate; }
    public BigDecimal receivedQty() { return receivedQty; }
}
