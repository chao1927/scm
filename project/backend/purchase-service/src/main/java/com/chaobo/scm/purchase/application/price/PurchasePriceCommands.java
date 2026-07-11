package com.chaobo.scm.purchase.application.price;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class PurchasePriceCommands {
    private PurchasePriceCommands() {
    }

    public record Create(
            long supplierId,
            String skuCode,
            long purchaseOrgId,
            int priceType,
            String currency,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String sourceType,
            String sourceNo) {
    }

    public record Version(int version) {
    }
}
