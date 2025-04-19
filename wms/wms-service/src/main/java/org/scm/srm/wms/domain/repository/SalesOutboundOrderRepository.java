package org.scm.srm.wms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.srm.wms.domain.model.SalesOutboundOrderAgg;

public interface SalesOutboundOrderRepository extends BaseRepository<SalesOutboundOrderAgg> {
    SalesOutboundOrderAgg findByOutboundNo(String outboundNo);
}
