package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.ReturnSupplyOutboundOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnSupplyOutboundOrderItemJpaRepository extends JpaRepository<ReturnSupplyOutboundOrderItem, Long> {
    List<ReturnSupplyOutboundOrderItem> findByOutboundNo(String outboundNo);
}
