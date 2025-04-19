package org.scm.pms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.pms.domain.model.ReturnSupplyDeliveryAgg;

public interface ReturnSupplyDeliveryRepository extends BaseRepository<ReturnSupplyDeliveryAgg> {
    ReturnSupplyDeliveryAgg findByDeliveryNo(String deliveryNo);
}
