package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.OmsOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OmsOrderItemJpaRepository extends JpaRepository<OmsOrderItem, Long> {
    List<OmsOrderItem> findByOmsOrderNo(String omsOrderNo);
}
