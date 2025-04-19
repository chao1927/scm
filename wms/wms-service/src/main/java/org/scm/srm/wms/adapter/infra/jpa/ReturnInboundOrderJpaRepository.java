package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.ReturnInboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnInboundOrderJpaRepository extends JpaRepository<ReturnInboundOrder, Long> {
    ReturnInboundOrder findByInboundNo(String inboundNo);
}
