package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdNotAndUsername(Long userId, String username);

    Optional<User> findByIdNotAndPhone(Long userId, String phone);

    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);
}
