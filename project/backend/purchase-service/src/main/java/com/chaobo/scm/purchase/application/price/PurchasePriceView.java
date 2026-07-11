package com.chaobo.scm.purchase.application.price;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchasePriceView(
        long id,
        String priceNo,
        long supplierId,
        String skuCode,
        long purchaseOrgId,
        int priceType,
        String currency,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal taxIncludedPrice,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String sourceType,
        String sourceNo,
        int status,
        String statusName,
        int version) {
}
