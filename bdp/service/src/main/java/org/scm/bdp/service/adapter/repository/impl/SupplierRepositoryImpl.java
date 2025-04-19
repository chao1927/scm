package org.scm.bdp.service.adapter.repository.impl;

import org.scm.bdp.service.adapter.infra.jpa.SupplierJpaRepository;
import org.scm.bdp.service.domain.model.SupplierAgg;
import org.scm.bdp.service.domain.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SupplierRepositoryImpl implements SupplierRepository {
    @Autowired
    private SupplierJpaRepository supplierJpaRepository;
    @Override
    public void save(SupplierAgg supplierAgg) {
        // TODO: 实现保存逻辑
    }
    @Override
    public SupplierAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}