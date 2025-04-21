package org.scm.bdp.service.adapter.repository.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.scm.bdp.service._share.enums.errorcode.SupplierErrorCode;
import org.scm.bdp.service.adapter.infra.jpa.SupplierCategoryJpaRepository;
import org.scm.bdp.service.adapter.infra.jpa.SupplierJpaRepository;
import org.scm.bdp.service.domain.model.SupplierCategoryAgg;
import org.scm.bdp.service.domain.repository.SupplierCategoryRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierCategoryRepositoryImpl implements SupplierCategoryRepository {

    @Autowired
    private SupplierCategoryJpaRepository supplierCategoryJpaRepository;

    @Autowired
    private SupplierJpaRepository supplierJpaRepository;

    @Override
    public void checkNameExist(String name) {
        supplierCategoryJpaRepository.findByName(name).ifPresent(supplierCategory -> {
            throw new BizException(SupplierErrorCode.SUPPLIER_CATEGORY_NAME_EXIST);
        });
    }

    @Override
    public void checkNameDuplicate(Long id, String name) {
        supplierCategoryJpaRepository.findByIdNotAndName(id, name).ifPresent(supplierCategory -> {
            throw new BizException(SupplierErrorCode.SUPPLIER_CATEGORY_NAME_DUPLICATE);
        });
    }

    @Override
    public void checkExistById(Long id) {
        // 判断供应商分类是否存在
        supplierCategoryJpaRepository.findById(id)
                .orElseThrow(() -> new BizException(SupplierErrorCode.SUPPLIER_CATEGORY_NOT_FOUND));

        // 判断供应商分类下是否存在供应商
        supplierJpaRepository.findByCategoryId(id).ifPresent(supplier -> {
            if (CollectionUtils.isNotEmpty(supplier)) {
                throw new BizException(SupplierErrorCode.SUPPLIER_CATEGORY_EXIST_SUPPLIER);
            }
        });
    }

    @Override
    public void save(SupplierCategoryAgg supplierCategoryAgg) {

    }

    @Override
    public SupplierCategoryAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
