package com.chaobo.scm.supplier.domain.returning;
import java.util.Optional;
public interface SupplierReturnRepository { Optional<SupplierReturnAggregate> findById(long id); void save(SupplierReturnAggregate aggregate,long operatorId); }
