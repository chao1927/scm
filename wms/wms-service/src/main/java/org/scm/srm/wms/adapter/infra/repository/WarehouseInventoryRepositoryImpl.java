package org.scm.srm.wms.adapter.infra.repository;

import org.scm.srm.wms.domain.model.WarehouseInventoryAgg;
import org.scm.srm.wms.domain.repository.WarehouseInventoryRepository;

public class WarehouseInventoryRepositoryImpl implements WarehouseInventoryRepository {
    @Override
    public void save(WarehouseInventoryAgg warehouseInventoryAgg) {

    }

    @Override
    public WarehouseInventoryAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public WarehouseInventoryAgg findByWarehouseIdAndSku(Long warehouseId, String sku) {
        return null;
    }
}
