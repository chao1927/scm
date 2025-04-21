package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.jpa.WarehouseJpaRepository;
import org.scm.bdp.service.domain.model.WarehouseAgg;
import org.scm.bdp.service.domain.repository.WarehouseRepository;
import org.scm.common.exception.BizException;
import org.scm.bdp.service._share.enums.errorcode.WarehouseErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WarehouseRepositoryImpl implements WarehouseRepository {
    @Autowired
    private WarehouseJpaRepository warehouseJpaRepository;
    @Override
    public void save(WarehouseAgg warehouseAgg) {
        warehouseJpaRepository.save(warehouseAgg.warehouse());
    }
    @Override
    public WarehouseAgg findById(Long id) {
        return warehouseJpaRepository.findById(id)
                .map(WarehouseAgg::new)
                .orElseThrow(() -> new BizException(WarehouseErrorCode.WAREHOUSE_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {
        warehouseJpaRepository.deleteById(id);
    }

    @Override
    public void checkNameExist(String name) {
        warehouseJpaRepository.findByName(name).ifPresent(warehouse -> {
            throw new BizException(WarehouseErrorCode.WAREHOUSE_NAME_EXIST);
        });
    }

    @Override
    public void checkNameDuplicate(Long id, String name) {
        warehouseJpaRepository.findByIdNotAndName(id, name).ifPresent(warehouse -> {
            throw new BizException(WarehouseErrorCode.WAREHOUSE_NAME_DUPLICATE);
        });
    }

    @Override
    public void checkExistById(Long id) {
        warehouseJpaRepository.findById(id)
                .orElseThrow(() -> new BizException(WarehouseErrorCode.WAREHOUSE_NOT_FOUND));
    }
}