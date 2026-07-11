package com.chaobo.scm.purchase.domain.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RfqLine {
    private final long lineId;
    private final String skuCode;
    private final BigDecimal targetQty;
    private final String uom;
    private final LocalDate requiredDeliveryDate;
    private final String qualityRequirement;

    public RfqLine(
            long lineId,
            String skuCode,
            BigDecimal targetQty,
            String uom,
            LocalDate requiredDeliveryDate,
            String qualityRequirement) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价SKU不能为空");
        }
        if (targetQty == null || targetQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价数量必须大于0");
        }
        if (uom == null || uom.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价单位不能为空");
        }
        this.lineId = lineId;
        this.skuCode = skuCode;
        this.targetQty = targetQty;
        this.uom = uom;
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.qualityRequirement = qualityRequirement;
    }

    public long lineId() {
        return lineId;
    }

    public String skuCode() {
        return skuCode;
    }

    public BigDecimal targetQty() {
        return targetQty;
    }

    public String uom() {
        return uom;
    }

    public LocalDate requiredDeliveryDate() {
        return requiredDeliveryDate;
    }

    public String qualityRequirement() {
        return qualityRequirement;
    }
}
