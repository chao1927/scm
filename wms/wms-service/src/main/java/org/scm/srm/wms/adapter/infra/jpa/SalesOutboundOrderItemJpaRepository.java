package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.SalesOutboundOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOutboundOrderItemJpaRepository extends JpaRepository<SalesOutboundOrderItem, Long> {
    List<SalesOutboundOrderItem> findByOutboundNo(String outboundNo);
}
