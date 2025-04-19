package org.scm.srm.adapter.infra.jpa;

import org.scm.srm.adapter.infra.domain.SupplierDeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierDeliveryOrderJpaRepository extends JpaRepository<SupplierDeliveryOrder, Long> {
    SupplierDeliveryOrder findByDeliveryNo(String deliveryNo);
}
