package org.scm.pms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.pms.domain.model.ReturnSupplyApplyAgg;

public interface ReturnSupplyApplyRepository extends BaseRepository<ReturnSupplyApplyAgg> {
    ReturnSupplyApplyAgg findByApplyNo(String applyNo);
}
