package com.chaobo.scm.purchase.domain.price;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchasePriceRepository {

    Optional<PurchasePriceAggregate> findByNo(String priceNo);

    List<PurchasePriceAggregate> findActiveOverlaps(
            long supplierId,
            String skuCode,
            long purchaseOrgId,
            String currency,
            LocalDate effectiveFrom,
            LocalDate effectiveTo);

    void save(PurchasePriceAggregate aggregate, long operatorId);
}
