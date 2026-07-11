package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AsnLine(
        long lineId,
        String skuCode,
        BigDecimal plannedQuantity,
        BigDecimal receivedQuantity,
        String batchNo,
        LocalDate productionDate,
        LocalDate expireDate) {

    public AsnLine {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN 行 SKU 不能为空");
        }
        if (plannedQuantity == null || plannedQuantity.signum() <= 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN 行计划发货数量必须大于 0");
        }
        if (productionDate != null && expireDate != null && !expireDate.isAfter(productionDate)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "失效日期必须晚于生产日期");
        }
    }
}
