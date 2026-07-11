package com.chaobo.scm.supplier.application.asn;

import java.time.OffsetDateTime;

public record AsnSummaryView(
        long asnId,
        String asnNo,
        long purchaseOrderId,
        long supplierId,
        long warehouseId,
        OffsetDateTime estimatedArrivalAt,
        int status,
        String statusName,
        int version,
        OffsetDateTime updatedAt) {}
