package com.chaobo.scm.purchase.application.order;

import com.chaobo.scm.common.api.PageResult;

import java.util.Optional;

public interface PurchaseOrderReadModelPort {
    PageResult<PurchaseOrderView> page(Long purchaseOrgId, Long supplierId, Integer status, int pageNo, int pageSize);
    Optional<PurchaseOrderView> detail(String orderNo);
}
