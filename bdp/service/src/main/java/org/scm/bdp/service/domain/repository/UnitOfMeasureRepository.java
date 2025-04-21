package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.UnitOfMeasureAgg;
import org.scm.common.BaseRepository;

public interface UnitOfMeasureRepository extends BaseRepository<UnitOfMeasureAgg> {
    void checkExistById(Long unitId);
}
