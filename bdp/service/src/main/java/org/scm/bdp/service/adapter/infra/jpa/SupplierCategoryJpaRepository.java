package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.SupplierCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierCategoryJpaRepository extends JpaRepository<SupplierCategory, Long> {
    Optional<SupplierCategory> findByName(String name);

    Optional<SupplierCategory> findByIdNotAndName(Long id, String name);
}