package org.scm.cis.adapter.infra.jpa;

import org.scm.cis.adapter.infra.domain.CentralWarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentralWarehouseInventoryRepository extends JpaRepository<CentralWarehouseInventory, Long> {
    CentralWarehouseInventory findByWarehouseIdAndSku(Long warehouseId, String sku);
}
