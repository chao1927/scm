package org.scm.srm.wms.adapter.infra.repository;

import org.scm.srm.wms.domain.model.LocationInventoryAgg;
import org.scm.srm.wms.domain.repository.LocationInventoryRepository;

public class LocationInventoryRepositoryImpl implements LocationInventoryRepository {
    @Override
    public void save(LocationInventoryAgg locationInventoryAgg) {

    }

    @Override
    public LocationInventoryAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public LocationInventoryAgg findByLocationAndSku(Long locationId, String sku, String batchNo) {
        return null;
    }
}
