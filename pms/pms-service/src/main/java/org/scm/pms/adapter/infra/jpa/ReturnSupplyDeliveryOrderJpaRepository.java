package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.ReturnSupplyDeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnSupplyDeliveryOrderJpaRepository extends JpaRepository<ReturnSupplyDeliveryOrder, Long> {
    ReturnSupplyDeliveryOrder findByDeliveryNo(String deliveryNo);
}
