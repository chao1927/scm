package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemJpaRepository extends JpaRepository<PurchaseOrderItem, Long> {
    List<PurchaseOrderItem> findByOrderNo(String orderNo);
}
