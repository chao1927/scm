package org.scm.oms.adapter.infra.jpa;

import org.scm.oms.adapter.infra.domain.EcommOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcommOrderItemJpaRepository extends JpaRepository<EcommOrderItem, Long> {
    List<EcommOrderItem> findByPlatformOrderNo(String platformOrderNo);
}
