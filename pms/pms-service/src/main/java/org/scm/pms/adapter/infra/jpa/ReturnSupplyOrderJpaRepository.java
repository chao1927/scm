package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.ReturnSupplyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnSupplyOrderJpaRepository extends JpaRepository<ReturnSupplyOrder, Long> {
    ReturnSupplyOrder findByOrderNo(String orderNo);
}
