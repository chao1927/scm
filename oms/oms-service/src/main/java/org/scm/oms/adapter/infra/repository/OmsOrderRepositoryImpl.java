package org.scm.oms.adapter.infra.repository;

import org.scm.oms.domain.model.OmsOrderAgg;
import org.scm.oms.domain.repository.OmsOrderRepository;

public class OmsOrderRepositoryImpl implements OmsOrderRepository {
    @Override
    public void save(OmsOrderAgg omsOrderAgg) {

    }

    @Override
    public OmsOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public OmsOrderAgg findByOrderNo(String omsOrderNo) {
        return null;
    }
}
