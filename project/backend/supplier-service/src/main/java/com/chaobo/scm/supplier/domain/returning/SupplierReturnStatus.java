package com.chaobo.scm.supplier.domain.returning;

import com.chaobo.scm.common.error.*;
import java.util.Arrays;

public enum SupplierReturnStatus {
    DRAFT(0, "草稿"), PENDING_REVIEW(1, "待审核"), APPROVED(2, "已审核"),
    INVENTORY_LOCKING(3, "库存锁定中"), PENDING_SUPPLIER_CONFIRMATION(4, "待供应商确认"),
    PENDING_OUTBOUND(5, "待出库"), OUTBOUNDED(6, "已出库"), IN_TRANSIT(7, "在途"),
    SIGNED(8, "已签收"), SUPPLIER_DIFFERENCE(9, "供应商差异"), CLOSED(10, "已关闭"),
    EXCEPTION_CLOSED(11, "异常关闭");
    private final int code; private final String label;
    SupplierReturnStatus(int code,String label){this.code=code;this.label=label;}
    public int code(){return code;} public String label(){return label;}
    public static SupplierReturnStatus fromCode(int code){return Arrays.stream(values()).filter(v->v.code==code).findFirst().orElseThrow(()->new BusinessException(ErrorCode.VALIDATION_FAILED,"退供状态不合法"));}
}
