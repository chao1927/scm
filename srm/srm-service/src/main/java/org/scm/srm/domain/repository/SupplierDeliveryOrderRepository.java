package org.scm.srm.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.srm.domain.model.SupplierDeliveryOrderAgg;

public interface SupplierDeliveryOrderRepository extends BaseRepository<SupplierDeliveryOrderAgg> {
    SupplierDeliveryOrderAgg findByDeliveryNo(String deliveryNo);
}
