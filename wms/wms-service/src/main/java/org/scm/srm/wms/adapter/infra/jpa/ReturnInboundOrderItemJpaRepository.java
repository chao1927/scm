package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.ReturnInboundOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnInboundOrderItemJpaRepository extends JpaRepository<ReturnInboundOrderItem, Long> {
    List<ReturnInboundOrderItem> findByInboundNo(String inboundNo);
}
