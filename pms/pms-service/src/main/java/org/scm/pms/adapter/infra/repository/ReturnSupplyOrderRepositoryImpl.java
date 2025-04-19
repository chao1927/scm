package org.scm.pms.adapter.infra.repository;

import org.scm.pms.domain.model.ReturnSupplyOrderAgg;
import org.scm.pms.domain.repository.ReturnSupplyOrderRepository;

public class ReturnSupplyOrderRepositoryImpl implements ReturnSupplyOrderRepository {
    @Override
    public void save(ReturnSupplyOrderAgg returnSupplyOrderAgg) {

    }

    @Override
    public ReturnSupplyOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public ReturnSupplyOrderAgg findByOrderNo(String orderNo) {
        return null;
    }
}
