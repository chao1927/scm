package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.ProductCategoryAgg;
import org.scm.common.BaseRepository;

public interface ProductCategoryRepository extends BaseRepository<ProductCategoryAgg> {

    void checkNameExist(String name);

    void checkExistById(Long parentId);

    void checkNameDuplicate(Long id, String name);

    void checkExistByParentId(Long id);

}
