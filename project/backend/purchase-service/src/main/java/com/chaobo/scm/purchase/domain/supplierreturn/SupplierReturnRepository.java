package com.chaobo.scm.purchase.domain.supplierreturn;

import java.util.Optional;

public interface SupplierReturnRepository {
    Optional<SupplierReturnAggregate> findByNo(String returnNo);
    void save(SupplierReturnAggregate aggregate, long operatorId);
}
