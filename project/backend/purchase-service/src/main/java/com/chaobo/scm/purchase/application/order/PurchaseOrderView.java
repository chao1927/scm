package com.chaobo.scm.purchase.application.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PurchaseOrderView(long id, String orderNo, int purchaseType, long supplierId, String supplierCode,
                                String supplierName, long purchaseOrgId, String warehouseCode, String currency,
                                BigDecimal totalAmount, BigDecimal taxAmount, BigDecimal taxIncludedAmount,
                                int status, String statusName, int versionNo, int version, OffsetDateTime releasedAt,
                                String cancelReason, List<Line> lines) {
    public record Line(long lineId, String skuCode, String skuName, BigDecimal orderQty, BigDecimal unitPrice,
                       BigDecimal taxRate, BigDecimal taxIncludedPrice, LocalDate requiredDeliveryDate,
                       BigDecimal receivedQty) {
    }
}
