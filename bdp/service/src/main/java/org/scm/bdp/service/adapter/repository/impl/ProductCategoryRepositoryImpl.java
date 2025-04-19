package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.domain.model.ProductCategoryAgg;
import org.scm.bdp.service.domain.repository.ProductCategoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {
    @Override
    public void save(ProductCategoryAgg productCategoryAgg) {
        // TODO: 实现保存逻辑
    }
    @Override
    public ProductCategoryAgg findById(Long id) {
        // TODO: 实现查询逻辑
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}