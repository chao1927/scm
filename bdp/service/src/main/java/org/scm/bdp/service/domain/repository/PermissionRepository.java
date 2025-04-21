package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.PermissionAgg;
import org.scm.common.BaseRepository;

import java.util.List;

public interface PermissionRepository extends BaseRepository<PermissionAgg> {
    void checkNameDuplicate(Long permissionId, String name);

    void checkCodeDuplicate(Long permissionId, String code);

    void checkExistById(Long id);

    void checkExistByIds(List<Long> permissionIds);

    void checkNameExist(String name);

    void checkCodeExist(String code);
}
