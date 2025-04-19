package org.scm.srm.adapter.infra.jpa;

import org.scm.srm.adapter.infra.domain.SupplierDeliveryOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierDeliveryOrderItemJpaRepository extends JpaRepository<SupplierDeliveryOrderItem, Long> {
    List<SupplierDeliveryOrderItem> findByDeliveryNo(String deliveryNo);
}
