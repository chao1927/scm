package org.scm.tms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.tms.domain.model.LogisticsOrderAgg;

public interface LogisticsOrderRepository extends BaseRepository<LogisticsOrderAgg> {
    LogisticsOrderAgg findByLogisticsNo(String logisticsNo);
}
