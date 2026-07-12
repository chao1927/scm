package com.chaobo.scm.purchase.application.supplierconfirm;

import java.time.OffsetDateTime;

public record SupplierConfirmView(
        long confirmId,
        String eventCode,
        String orderNo,
        long supplierId,
        String confirmStatus,
        String reason,
        int sourceVersion,
        int processedStatus,
        String processedStatusName,
        String processComment,
        long purchaseOrgId,
        int version,
        OffsetDateTime occurredAt,
        OffsetDateTime processedAt,
        String payloadJson) {
}
