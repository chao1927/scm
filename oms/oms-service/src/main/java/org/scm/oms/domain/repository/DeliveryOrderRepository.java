package org.scm.oms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.oms.domain.model.DeliveryOrderAgg;

public interface DeliveryOrderRepository extends BaseRepository<DeliveryOrderAgg> {
    DeliveryOrderAgg findByDeliveryNo(String deliveryNo);
}
