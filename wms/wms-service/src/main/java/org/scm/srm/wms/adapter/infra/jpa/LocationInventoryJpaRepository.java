package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.LocationInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationInventoryJpaRepository extends JpaRepository<LocationInventory, Long> {
    LocationInventory findByWarehouseIdAndLocationIdAndSku(Long warehouseId, Long locationId, String sku);
}
