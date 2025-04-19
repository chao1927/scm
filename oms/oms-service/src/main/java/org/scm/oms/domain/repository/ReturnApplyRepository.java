package org.scm.oms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.oms.domain.model.ReturnApplyAgg;

public interface ReturnApplyRepository extends BaseRepository<ReturnApplyAgg> {
    ReturnApplyAgg findByApplyNo(String returnApplyNo);

}
