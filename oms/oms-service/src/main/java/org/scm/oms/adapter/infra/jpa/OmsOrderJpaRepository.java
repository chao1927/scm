package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.OmsOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OmsOrderJpaRepository extends JpaRepository<OmsOrder, Long> {
    OmsOrder findByOmsOrderNo(String omsOrderNo);
}
