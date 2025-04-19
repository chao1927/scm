package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrder, Long> {
    PurchaseOrder findByOrderNo(String orderNo);
}
