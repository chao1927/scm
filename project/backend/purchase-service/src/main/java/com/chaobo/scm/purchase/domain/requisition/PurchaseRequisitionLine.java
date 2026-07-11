package com.chaobo.scm.purchase.domain.requisition;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseRequisitionLine {
    private final long lineId;
    private final String skuCode;
    private BigDecimal requestedQty;
    private BigDecimal approvedQty;
    private BigDecimal convertedQty;
    private final String purchaseUnit;
    private final LocalDate requiredDate;
    private final String remark;

    public PurchaseRequisitionLine(
            long lineId,
            String skuCode,
            BigDecimal requestedQty,
            BigDecimal approvedQty,
            BigDecimal convertedQty,
            String purchaseUnit,
            LocalDate requiredDate,
            String remark) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "SKU不能为空");
        }
        if (requestedQty == null || requestedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "请购数量必须大于0");
        }
        if (requiredDate == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "需求日期不能为空");
        }
        this.lineId = lineId;
        this.skuCode = skuCode;
        this.requestedQty = requestedQty;
        this.approvedQty = approvedQty == null ? BigDecimal.ZERO : approvedQty;
        this.convertedQty = convertedQty == null ? BigDecimal.ZERO : convertedQty;
        this.purchaseUnit = purchaseUnit;
        this.requiredDate = requiredDate;
        this.remark = remark;
    }

    public void approve(BigDecimal quantity) {
        if (quantity == null || quantity.signum() < 0 || quantity.compareTo(requestedQty) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "批准数量必须在0到请购数量之间");
        }
        this.approvedQty = quantity;
    }

    public void changeRequestedQty(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "请购数量必须大于0");
        }
        if (quantity.compareTo(convertedQty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "请购数量不能小于已转采购数量");
        }
        this.requestedQty = quantity;
        if (approvedQty.compareTo(quantity) > 0) {
            this.approvedQty = quantity;
        }
    }

    public void convert(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "转采购数量必须大于0");
        }
        if (convertedQty.add(quantity).compareTo(approvedQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "转采购数量不能超过已批准未转数量");
        }
        this.convertedQty = convertedQty.add(quantity);
    }

    public BigDecimal remainingApprovedQty() {
        return approvedQty.subtract(convertedQty);
    }

    public long lineId() {
        return lineId;
    }

    public String skuCode() {
        return skuCode;
    }

    public BigDecimal requestedQty() {
        return requestedQty;
    }

    public BigDecimal approvedQty() {
        return approvedQty;
    }

    public BigDecimal convertedQty() {
        return convertedQty;
    }

    public String purchaseUnit() {
        return purchaseUnit;
    }

    public LocalDate requiredDate() {
        return requiredDate;
    }

    public String remark() {
        return remark;
    }
}
