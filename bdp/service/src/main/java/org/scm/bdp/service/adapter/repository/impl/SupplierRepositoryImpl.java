package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service._share.enums.errorcode.SupplierErrorCode;
import org.scm.bdp.service.adapter.infra.jpa.SupplierJpaRepository;
import org.scm.bdp.service.domain.model.SupplierAgg;
import org.scm.bdp.service.domain.repository.SupplierRepository;
import org.scm.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class SupplierRepositoryImpl implements SupplierRepository {
    @Autowired
    private SupplierJpaRepository supplierJpaRepository;
    @Override
    public void save(SupplierAgg supplierAgg) {
        supplierJpaRepository.save(supplierAgg.supplier());
    }
    @Override
    public SupplierAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {
        supplierJpaRepository.deleteById(id);
    }

    @Override
    public void checkNameExist(String name) {
        supplierJpaRepository.findByName(name).ifPresent(supplier -> {
            throw new BizException(SupplierErrorCode.SUPPLIER_NAME_EXIST);
        });
    }

    @Override
    public void checkExistById(Long id) {
        supplierJpaRepository.findById(id).orElseThrow(() -> {
            throw new BizException(SupplierErrorCode.SUPPLIER_NOT_FOUND);
        });
    }

    @Override
    public void checkNameDuplicate(Long id, String name) {
        supplierJpaRepository.findByIdNotAndName(id, name).ifPresent(supplier -> {
            throw new BizException(SupplierErrorCode.SUPPLIER_NAME_EXIST);
        });
    }
}