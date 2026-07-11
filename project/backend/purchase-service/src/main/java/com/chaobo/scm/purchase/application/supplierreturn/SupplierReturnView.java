package com.chaobo.scm.purchase.application.supplierreturn;

import java.math.BigDecimal;
import java.util.List;

public record SupplierReturnView(long id, String returnNo, String sourceOrderNo, long supplierId, long purchaseOrgId,
                                 String warehouseCode, int status, String statusName, String rejectReason, int version,
                                 List<Line> lines) {
    public record Line(long lineId, String skuCode, BigDecimal returnQty, BigDecimal returnableQty, String reason) {
    }
}
