package com.chaobo.scm.purchase.application.orderchange;

import com.chaobo.scm.common.api.PageResult;

import java.util.Optional;

public interface PurchaseOrderChangeReadModelPort {
    PageResult<PurchaseOrderChangeView> page(String orderNo, Integer status, int pageNo, int pageSize);
    Optional<PurchaseOrderChangeView> detail(String changeNo);
}
