package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import java.time.OffsetDateTime;

public record AsnRow(
        long asnId,
        String asnNo,
        long purchaseOrderId,
        long supplierId,
        long warehouseId,
        OffsetDateTime eta,
        OffsetDateTime shipAt,
        String carrierName,
        String trackingNo,
        int asnStatus,
        String cancelReason,
        int version) {}
