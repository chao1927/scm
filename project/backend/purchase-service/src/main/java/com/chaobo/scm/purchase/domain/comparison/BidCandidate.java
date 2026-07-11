package com.chaobo.scm.purchase.domain.comparison;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BidCandidate {
    private final long candidateId;
    private final long supplierId;
    private final String supplierName;
    private final String quoteNo;
    private final String skuCode;
    private final BigDecimal quoteQty;
    private final BigDecimal unitPrice;
    private final BigDecimal taxRate;
    private final int deliveryDays;
    private final BigDecimal supplierScore;
    private final BigDecimal transportScore;
    private final BigDecimal estimatedFreightCost;
    private final BigDecimal totalCost;
    private final BigDecimal compositeScore;
    private boolean awarded;

    public BidCandidate(
            long candidateId,
            long supplierId,
            String supplierName,
            String quoteNo,
            String skuCode,
            BigDecimal quoteQty,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            int deliveryDays,
            BigDecimal supplierScore,
            BigDecimal transportScore,
            BigDecimal estimatedFreightCost,
            boolean awarded) {
        if (supplierId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选供应商不能为空");
        }
        if (quoteNo == null || quoteNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选报价单号不能为空");
        }
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选SKU不能为空");
        }
        if (quoteQty == null || quoteQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选报价数量必须大于0");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选报价单价不能小于0");
        }
        if (deliveryDays < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "交期天数不能小于0");
        }
        this.candidateId = candidateId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.quoteNo = quoteNo;
        this.skuCode = skuCode;
        this.quoteQty = quoteQty;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate == null ? BigDecimal.ZERO : taxRate;
        this.deliveryDays = deliveryDays;
        this.supplierScore = defaultScore(supplierScore);
        this.transportScore = defaultScore(transportScore);
        this.estimatedFreightCost = estimatedFreightCost == null ? BigDecimal.ZERO : estimatedFreightCost;
        this.totalCost = unitPrice.multiply(quoteQty).add(this.estimatedFreightCost).setScale(6, RoundingMode.HALF_UP);
        this.compositeScore = calculateScore();
        this.awarded = awarded;
    }

    public void award() {
        this.awarded = true;
    }

    public void clearAward() {
        this.awarded = false;
    }

    private BigDecimal calculateScore() {
        var priceScore = BigDecimal.valueOf(1000)
                .divide(totalCost.add(BigDecimal.ONE), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        var deliveryScore = BigDecimal.valueOf(Math.max(0, 100 - deliveryDays));
        return priceScore.multiply(BigDecimal.valueOf(0.45))
                .add(supplierScore.multiply(BigDecimal.valueOf(0.25)))
                .add(transportScore.multiply(BigDecimal.valueOf(0.2)))
                .add(deliveryScore.multiply(BigDecimal.valueOf(0.1)))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal defaultScore(BigDecimal value) {
        return value == null ? BigDecimal.valueOf(60) : value;
    }

    public long candidateId() {
        return candidateId;
    }

    public long supplierId() {
        return supplierId;
    }

    public String supplierName() {
        return supplierName;
    }

    public String quoteNo() {
        return quoteNo;
    }

    public String skuCode() {
        return skuCode;
    }

    public BigDecimal quoteQty() {
        return quoteQty;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public BigDecimal taxRate() {
        return taxRate;
    }

    public int deliveryDays() {
        return deliveryDays;
    }

    public BigDecimal supplierScore() {
        return supplierScore;
    }

    public BigDecimal transportScore() {
        return transportScore;
    }

    public BigDecimal estimatedFreightCost() {
        return estimatedFreightCost;
    }

    public BigDecimal totalCost() {
        return totalCost;
    }

    public BigDecimal compositeScore() {
        return compositeScore;
    }

    public boolean awarded() {
        return awarded;
    }
}
