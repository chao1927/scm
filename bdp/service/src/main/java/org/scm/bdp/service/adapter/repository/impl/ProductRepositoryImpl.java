package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.jpa.ProductJpaRepository;
import org.scm.bdp.service.domain.model.ProductAgg;
import org.scm.bdp.service.domain.repository.ProductRepository;
import org.scm.common.exception.BizException;
import org.scm.common.exception.ProductErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Override
    public void save(ProductAgg productAgg) {
        // TODO: 实现保存逻辑
    }
    @Override
    public ProductAgg findById(Long id) {
        return productJpaRepository.findById(id)
                .map(ProductAgg::new)
                .orElseThrow(() -> new BizException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {

    }
}