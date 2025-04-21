package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.SupplierCategoryAgg;
import org.scm.common.BaseRepository;

public interface SupplierCategoryRepository extends BaseRepository<SupplierCategoryAgg> {
    void checkNameExist(String name);

    void checkNameDuplicate(Long id, String name);

    void checkExistById(Long id);
}
