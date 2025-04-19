package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.jpa.WarehouseJpaRepository;
import org.scm.bdp.service.domain.model.WarehouseAgg;
import org.scm.bdp.service.domain.repository.WarehouseRepository;
import org.scm.common.exception.BizException;
import org.scm.common.exception.WarehouseErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WarehouseRepositoryImpl implements WarehouseRepository {
    @Autowired
    private WarehouseJpaRepository warehouseJpaRepository;
    @Override
    public void save(WarehouseAgg warehouseAgg) {
        // TODO: 实现保存逻辑
    }
    @Override
    public WarehouseAgg findById(Long id) {
        return warehouseJpaRepository.findById(id)
                .map(w -> new WarehouseAgg(w, null, null))
                .orElseThrow(() -> new BizException(WarehouseErrorCode.WAREHOUSE_NOT_FOUND));
    }

    @Override
    public void deleteById(Long id) {

    }
}