package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import java.time.OffsetDateTime;

public record AsnSummaryRow(
        long asnId,
        String asnNo,
        long purchaseOrderId,
        long supplierId,
        long warehouseId,
        OffsetDateTime eta,
        int asnStatus,
        int version,
        OffsetDateTime updatedAt) {}
