package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByIdNotAndName(Long roleId, String name);

    Optional<Role> findByName(String name);

    Optional<Role> findByIdNotAndCode(Long roleId, String code);

    Optional<Role> findByCode(String code);
}
