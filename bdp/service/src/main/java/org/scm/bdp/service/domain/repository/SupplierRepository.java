package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.SupplierAgg;
import org.scm.common.BaseRepository;

public interface SupplierRepository extends BaseRepository<SupplierAgg> {
    void checkNameExist(String name);

    void checkExistById(Long id);

    void checkNameDuplicate(Long id, String name);
}
