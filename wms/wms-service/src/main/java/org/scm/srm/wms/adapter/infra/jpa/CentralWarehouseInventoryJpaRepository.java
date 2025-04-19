package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.CentralWarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentralWarehouseInventoryJpaRepository extends JpaRepository<CentralWarehouseInventory, Long> {
    CentralWarehouseInventory findByWarehouseIdAndSku(Long warehouseId, String sku);
}
