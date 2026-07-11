package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.common.api.PageResult;

public interface AsnReadModelPort {
    PageResult<AsnSummaryView> page(Long supplierId, Integer status, String keyword, int pageNo, int pageSize);
}
