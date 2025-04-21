package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.StorageLocationAgg;
import org.scm.common.BaseRepository;

public interface StorageLocationRepository extends BaseRepository<StorageLocationAgg> {
    void checkCodeExist(String code);

    void checkCodeDuplicate(Long id, String code);

    void checkExistById(Long locationId);

}
