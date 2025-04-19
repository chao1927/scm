package org.scm.srm.wms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.srm.wms.domain.model.ReturnSupplyOutboundOrderAgg;

public interface ReturnSupplyOutboundOrderRepository extends BaseRepository<ReturnSupplyOutboundOrderAgg> {
    ReturnSupplyOutboundOrderAgg findByOutboundNo(String outboundNo);
}
