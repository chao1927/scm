package org.scm.srm.wms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.srm.wms.domain.model.WarehouseInventoryAgg;

public interface WarehouseInventoryRepository extends BaseRepository<WarehouseInventoryAgg> {
    WarehouseInventoryAgg findByWarehouseIdAndSku(Long warehouseId, String sku);
}
