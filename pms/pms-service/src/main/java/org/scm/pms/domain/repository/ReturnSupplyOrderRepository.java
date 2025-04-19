package org.scm.pms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.pms.domain.model.ReturnSupplyOrderAgg;

public interface ReturnSupplyOrderRepository extends BaseRepository<ReturnSupplyOrderAgg> {
    ReturnSupplyOrderAgg findByOrderNo(String orderNo);
}
