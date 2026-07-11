package com.chaobo.scm.purchase.application.comparison;

import com.chaobo.scm.common.api.PageResult;

import java.util.Optional;

public interface BidComparisonReadModelPort {

    PageResult<BidComparisonView> page(Long purchaseOrgId, Integer status, String rfqNo, int pageNo, int pageSize);

    Optional<BidComparisonView> detail(String compareNo);
}
