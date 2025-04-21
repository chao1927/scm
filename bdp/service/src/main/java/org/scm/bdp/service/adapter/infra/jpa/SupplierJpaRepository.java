package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierJpaRepository extends JpaRepository<Supplier, Long> {

    Optional<List<Supplier>> findByCategoryId(Long categoryId);

    Optional<Supplier> findByName(String name);

    Optional<Supplier> findByIdNotAndName(Long id, String name);
}