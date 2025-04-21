package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.WarehouseAgg;
import org.scm.common.BaseRepository;

public interface WarehouseRepository extends BaseRepository<WarehouseAgg> {
    void checkNameExist(String name);

    void checkNameDuplicate(Long id, String name);

    void checkExistById(Long id);
}
