package org.scm.bdp.service.adapter.repository.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.scm.bdp.service._share.enums.errorcode.ProductErrorCode;
import org.scm.bdp.service.adapter.infra.jpa.ProductCategoryJpaRepository;
import org.scm.bdp.service.domain.model.ProductCategoryAgg;
import org.scm.bdp.service.domain.repository.ProductCategoryRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    @Autowired
    private ProductCategoryJpaRepository productCategoryJpaRepository;


    @Override
    public void save(ProductCategoryAgg productCategoryAgg) {
        productCategoryJpaRepository.save(productCategoryAgg.category());
    }
    @Override
    public ProductCategoryAgg findById(Long id) {
        return productCategoryJpaRepository.findById(id)
                .map(ProductCategoryAgg::new)
                .orElseThrow(() -> new BizException(ProductErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        // 查看是否有子分类
        checkExistByParentId(id);

        productCategoryJpaRepository.deleteById(id);
    }

    @Override
    public void checkNameExist(String name) {
        productCategoryJpaRepository.findByName(name).ifPresent(productCategory -> {
            throw new BizException(ProductErrorCode.PRODUCT_CATEGORY_NAME_EXIST);
        });
    }

    @Override
    public void checkExistById(Long id) {
        productCategoryJpaRepository.findById(id)
                .orElseThrow(() -> new BizException(ProductErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }

    @Override
    public void checkNameDuplicate(Long id, String name) {
        productCategoryJpaRepository.findByIdNotAndName(id, name)
                .ifPresent((category) -> {
                    throw new BizException(ProductErrorCode.PRODUCT_CATEGORY_NAME_DUPLICATE);
                });
    }

    @Override
    public void checkExistByParentId(Long id) {
        productCategoryJpaRepository.findByParentId(id).ifPresent(categories -> {
            if (CollectionUtils.isNotEmpty(categories)) throw new BizException(ProductErrorCode.PRODUCT_CATEGORY_HAS_CHILD);
        });
    }

}