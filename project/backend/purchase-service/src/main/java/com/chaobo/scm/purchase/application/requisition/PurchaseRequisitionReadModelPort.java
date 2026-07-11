package com.chaobo.scm.purchase.application.requisition;

import com.chaobo.scm.common.api.PageResult;

import java.util.Optional;

public interface PurchaseRequisitionReadModelPort {

    PageResult<PurchaseRequisitionView> page(
            Long purchaseOrgId,
            Integer status,
            String keyword,
            int pageNo,
            int pageSize);

    Optional<PurchaseRequisitionView> detail(long id);
}
