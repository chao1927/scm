package com.chaobo.scm.purchase.application.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PurchaseOrderCommands {
    private PurchaseOrderCommands() {
    }

    public record Create(int purchaseType, long supplierId, String supplierCode, String supplierName,
                         long purchaseOrgId, String warehouseCode, String currency, List<Line> lines) {
    }

    public record Line(Long lineId, String skuCode, String skuName, BigDecimal orderQty, BigDecimal unitPrice,
                       BigDecimal taxRate, LocalDate requiredDeliveryDate) {
    }

    public record Version(int version) {
    }

    public record Approve(int version, boolean approved, String reason) {
    }

    public record Publish(int version, String publishMode) {
    }

    public record Cancel(int version, String reason) {
    }

    public record Close(int version, String reason) {
    }
}
