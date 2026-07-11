package com.chaobo.scm.purchase.application.supplierreturn;

import com.chaobo.scm.common.api.PageResult;

import java.util.Optional;

public interface SupplierReturnReadModelPort {
    PageResult<SupplierReturnView> page(Long purchaseOrgId, Long supplierId, String warehouseCode, Integer status,
                                        int pageNo, int pageSize);
    Optional<SupplierReturnView> detail(String returnNo);
}
