package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.DeliveryOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryOrderItemJpaRepository extends JpaRepository<DeliveryOrderItem, Long> {
    List<DeliveryOrderItem> findByDeliveryNo(String deliveryNo);
}
