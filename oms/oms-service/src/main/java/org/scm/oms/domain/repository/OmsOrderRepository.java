package org.scm.oms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.oms.domain.model.OmsOrderAgg;

public interface OmsOrderRepository extends BaseRepository<OmsOrderAgg> {
    OmsOrderAgg findByOrderNo(String omsOrderNo);
}
