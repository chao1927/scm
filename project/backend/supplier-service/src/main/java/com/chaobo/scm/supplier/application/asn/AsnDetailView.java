package com.chaobo.scm.supplier.application.asn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record AsnDetailView(
        long asnId,
        String asnNo,
        long purchaseOrderId,
        long supplierId,
        long warehouseId,
        OffsetDateTime estimatedArrivalAt,
        Integer status,
        String statusName,
        OffsetDateTime shippedAt,
        String carrierName,
        String trackingNo,
        String cancelReason,
        int version,
        List<LineView> lines) {

    public AsnDetailView {
        lines = List.copyOf(lines);
    }

    public record LineView(long lineId, String skuCode, BigDecimal plannedQuantity,
                           BigDecimal receivedQuantity, String batchNo,
                           LocalDate productionDate, LocalDate expireDate) {}
}
