package com.chaobo.scm.purchase.application.comparison;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class BidComparisonCommands {
    private BidComparisonCommands() {
    }

    public record Generate(
            String rfqNo,
            long purchaseOrgId,
            String currency,
            List<Candidate> candidates) {
    }

    public record Candidate(
            Long candidateId,
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
            BigDecimal estimatedFreightCost) {
    }

    public record Award(
            int version,
            long candidateId,
            String reason,
            boolean activatePurchasePrice,
            int priceType,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
    }
}
