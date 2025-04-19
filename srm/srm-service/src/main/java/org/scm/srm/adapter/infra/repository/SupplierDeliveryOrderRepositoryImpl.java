package org.scm.srm.adapter.infra.repository;

import org.scm.srm.domain.model.SupplierDeliveryOrderAgg;
import org.scm.srm.domain.repository.SupplierDeliveryOrderRepository;
import org.springframework.stereotype.Component;

@Component
public class SupplierDeliveryOrderRepositoryImpl implements SupplierDeliveryOrderRepository {
    @Override
    public void save(SupplierDeliveryOrderAgg supplierDeliveryOrderAgg) {

    }

    @Override
    public SupplierDeliveryOrderAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public SupplierDeliveryOrderAgg findByDeliveryNo(String deliveryNo) {
        return null;
    }
}
