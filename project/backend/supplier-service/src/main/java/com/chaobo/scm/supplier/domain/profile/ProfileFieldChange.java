package com.chaobo.scm.supplier.domain.profile;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.util.Set;

public record ProfileFieldChange(String fieldCode, String beforeValue, String afterValue) {
    private static final Set<String> IMMUTABLE_FIELDS = Set.of("supplierId", "supplierCode", "lifecycleStatus");

    public ProfileFieldChange {
        if (fieldCode == null || fieldCode.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "变更字段编码不能为空");
        }
        if (IMMUTABLE_FIELDS.contains(fieldCode)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, fieldCode + " 不允许通过资料变更修改");
        }
        if (java.util.Objects.equals(beforeValue, afterValue)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, fieldCode + " 的新旧值不能相同");
        }
    }
}
