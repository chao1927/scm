package com.chaobo.scm.supplier.domain.item;

import com.chaobo.scm.common.error.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SupplyCondition(BigDecimal moq, BigDecimal mpq, int leadTimeDays, String purchaseUnit,
                              LocalDate effectiveFrom, LocalDate effectiveTo) {
    public SupplyCondition {
        if(moq==null||moq.signum()<=0||mpq==null||mpq.signum()<=0) throw rule("MOQ和MPQ必须大于0");
        if(leadTimeDays<0) throw rule("供货周期不能小于0");
        if(purchaseUnit==null||purchaseUnit.isBlank()) throw rule("采购单位不能为空");
        if(effectiveFrom!=null&&effectiveTo!=null&&effectiveTo.isBefore(effectiveFrom)) throw rule("供货失效日不能早于生效日");
    }
    private static BusinessException rule(String message){return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,message);}
}
