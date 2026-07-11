package com.chaobo.scm.supplier.domain.item;
import java.util.Optional;import java.util.List;
public interface SupplierItemRepository { Optional<SupplierItemAggregate> findById(long id); List<SupplierItemAggregate> findAvailableBySupplier(long supplierId); List<SupplierItemAggregate> findAvailableBySku(String skuCode); boolean exists(long supplierId,String skuCode); void save(SupplierItemAggregate aggregate,long operatorId); }
