package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service._share.enums.errorcode.ProductErrorCode;
import org.scm.bdp.service.adapter.infra.jpa.ProductJpaRepository;
import org.scm.bdp.service.domain.model.ProductAgg;
import org.scm.bdp.service.domain.repository.ProductRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Override
    public void save(ProductAgg productAgg) {
        productJpaRepository.save(productAgg.product());
    }

    @Override
    public ProductAgg findById(Long id) {
        return productJpaRepository.findById(id)
                .map(ProductAgg::new)
                .orElseThrow(() -> new BizException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        productJpaRepository.deleteById(id);
    }

    @Override
    public void checkExistByCategoryId(Long id) {
        productJpaRepository.countByCategoryId(id)
                .ifPresent(num -> {
                    if (num > 0) {
                        throw new BizException(ProductErrorCode.PRODUCT_CATEGORY_EXIST_PRODUCT);
                    }
                });
    }

    @Override
    public void checkExistById(Long id) {
        productJpaRepository.findById(id).orElseThrow(() -> new BizException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

}