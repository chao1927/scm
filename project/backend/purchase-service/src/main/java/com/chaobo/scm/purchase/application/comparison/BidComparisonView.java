package com.chaobo.scm.purchase.application.comparison;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BidComparisonView(
        long id,
        String compareNo,
        String rfqNo,
        long purchaseOrgId,
        String currency,
        int status,
        String statusName,
        Long awardedCandidateId,
        String decisionReason,
        Long decidedBy,
        OffsetDateTime decidedAt,
        int version,
        List<Candidate> candidates) {

    public record Candidate(
            long candidateId,
            long supplierId,
            String supplierName,
            String quoteNo,
            String skuCode,
            BigDecimal quoteQty,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            int deliveryDays,
            BigDecimal supplierScore,
            BigDecimal transportScore,
            BigDecimal estimatedFreightCost,
            BigDecimal totalCost,
            BigDecimal compositeScore,
            boolean awarded) {
    }
}
