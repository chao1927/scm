package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.time.OffsetDateTime;

public record ShipmentInfo(
        OffsetDateTime shippedAt,
        String carrierName,
        String trackingNo) {

    public ShipmentInfo {
        if (shippedAt == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "发货时间不能为空");
        }
        if (carrierName == null || carrierName.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "承运商不能为空");
        }
    }
}
