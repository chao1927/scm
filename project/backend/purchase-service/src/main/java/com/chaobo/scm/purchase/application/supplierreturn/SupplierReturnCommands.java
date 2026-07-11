package com.chaobo.scm.purchase.application.supplierreturn;

import java.math.BigDecimal;
import java.util.List;

public final class SupplierReturnCommands {
    private SupplierReturnCommands() {
    }

    public record Create(String sourceOrderNo, long supplierId, long purchaseOrgId, String warehouseCode,
                         List<Line> lines) {
    }

    public record Line(Long lineId, String skuCode, BigDecimal returnQty, BigDecimal returnableQty, String reason) {
    }

    public record Version(int version) {
    }

    public record Approve(int version, boolean approved, String reason) {
    }

    public record Notify(int version, String notifyMode) {
    }
}
