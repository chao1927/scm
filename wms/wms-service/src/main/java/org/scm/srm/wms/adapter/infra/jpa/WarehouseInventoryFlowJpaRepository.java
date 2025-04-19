package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.WarehouseInventoryFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseInventoryFlowJpaRepository extends JpaRepository<WarehouseInventoryFlow, Long> {
    List<WarehouseInventoryFlow> findByWarehouseId(Long warehouseId);
}
