package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.SalesReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesReturnOrderJpaRepository extends JpaRepository<SalesReturnOrder, Long> {
    SalesReturnOrder findBySalesReturnNo(String salesReturnNo);
}
