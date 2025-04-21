package org.scm.bdp.service.domain.repository;

import org.scm.bdp.service.domain.model.RoleAgg;
import org.scm.common.BaseRepository;

import java.util.List;

public interface RoleRepository extends BaseRepository<RoleAgg> {
    void checkNameDuplicate(Long roleId, String name);


    void checkCodeDuplicate(Long roleId, String code);

    void checkExistById(Long id);

    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    void checkNameExist(String name);

    void checkCodeExist(String code);

    void checkExistByIds(List<Long> roleIds);
}
