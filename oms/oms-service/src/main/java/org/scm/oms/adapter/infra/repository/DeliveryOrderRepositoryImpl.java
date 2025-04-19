package org.scm.oms.adapter.infra.repository;

import org.scm.oms.domain.model.DeliveryOrderAgg;
import org.scm.oms.domain.repository.DeliveryOrderRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DeliveryOrderRepositoryImpl implements DeliveryOrderRepository {


    @Override
    public void save(DeliveryOrderAgg deliveryOrderAgg) {

    }

    @Override
    public DeliveryOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public DeliveryOrderAgg findByDeliveryNo(String deliveryNo) {
        return null;
    }
}
