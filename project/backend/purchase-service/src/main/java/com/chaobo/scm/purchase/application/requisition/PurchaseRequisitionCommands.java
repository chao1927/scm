package com.chaobo.scm.purchase.application.requisition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class PurchaseRequisitionCommands {
    private PurchaseRequisitionCommands() {
    }

    public record Save(
            Long requisitionId,
            int version,
            long applicantId,
            long purchaseOrgId,
            long demandDepartmentId,
            String reason,
            List<Line> lines) {
    }

    public record Line(
            Long lineId,
            String skuCode,
            BigDecimal requestedQty,
            String purchaseUnit,
            LocalDate requiredDate,
            String remark) {
    }

    public record Approve(
            int version,
            Map<Long, BigDecimal> approvedQuantities) {
    }

    public record Reject(
            int version,
            String reason) {
    }

    public record Convert(
            int version,
            String targetType,
            String targetNo,
            Map<Long, BigDecimal> quantities) {
    }
}
