package org.scm.srm.wms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.srm.wms.adapter.infra.domain.LocationInventory;
import org.scm.srm.wms.domain.model.LocationInventoryAgg;

public interface LocationInventoryRepository extends BaseRepository<LocationInventoryAgg> {
    LocationInventoryAgg findByLocationAndSku(Long locationId, String sku, String batchNo);
}
