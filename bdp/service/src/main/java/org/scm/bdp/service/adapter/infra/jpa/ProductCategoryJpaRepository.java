package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategory, Long> {
    Optional<List<ProductCategory>> findByParentId(Long id);

    Optional<ProductCategory> findByName(String name);

    Optional<ProductCategory> findByIdNotAndName(Long id, String name);
}