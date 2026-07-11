package com.chaobo.scm.purchase.application.price;

import com.chaobo.scm.common.api.PageResult;

import java.util.Optional;

public interface PurchasePriceReadModelPort {

    PageResult<PurchasePriceView> page(
            Long purchaseOrgId,
            Long supplierId,
            String skuCode,
            String currency,
            Integer status,
            int pageNo,
            int pageSize);

    Optional<PurchasePriceView> detail(String priceNo);
}
