package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermission, Long> {
    Optional<List<RolePermission>> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);

    Optional<List<RolePermission>> findByPermissionId(Long permissionId);
}