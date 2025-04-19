package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    Product findBySku(String sku);

}