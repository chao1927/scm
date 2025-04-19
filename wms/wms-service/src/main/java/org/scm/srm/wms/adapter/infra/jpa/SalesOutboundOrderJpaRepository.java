package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.SalesOutboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesOutboundOrderJpaRepository extends JpaRepository<SalesOutboundOrder, Long> {
    SalesOutboundOrder findByOutboundNo(String outboundNo);
}
