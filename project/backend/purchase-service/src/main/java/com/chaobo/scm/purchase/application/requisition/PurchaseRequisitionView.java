package com.chaobo.scm.purchase.application.requisition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PurchaseRequisitionView(
        long id,
        String requisitionNo,
        long applicantId,
        long purchaseOrgId,
        long demandDepartmentId,
        int status,
        String statusName,
        String reason,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<Line> lines) {

    public record Line(
            long lineId,
            String skuCode,
            BigDecimal requestedQty,
            BigDecimal approvedQty,
            BigDecimal convertedQty,
            String purchaseUnit,
            LocalDate requiredDate,
            String remark) {
    }
}
