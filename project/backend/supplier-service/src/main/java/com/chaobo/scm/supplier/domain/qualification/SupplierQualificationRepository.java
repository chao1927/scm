package com.chaobo.scm.supplier.domain.qualification;
import java.util.Optional;
public interface SupplierQualificationRepository { Optional<SupplierQualificationAggregate> findById(long id); void save(SupplierQualificationAggregate aggregate,long operatorId); }
