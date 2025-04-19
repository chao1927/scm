package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryOrderJpaRepository extends JpaRepository<DeliveryOrder, Long> {
    DeliveryOrder findByDeliveryNo(String deliveryNo);
}
