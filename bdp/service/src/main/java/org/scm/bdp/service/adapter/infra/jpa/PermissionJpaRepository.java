package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PermissionJpaRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByIdNotAndName(Long permissionId, String name);

    Optional<Permission> findByIdNotAndCode(Long permissionId, String code);

    Optional<Permission> findByName(String name);

    Optional<Permission> findByCode(String code);


}
