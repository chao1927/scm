package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.LocationInventoryFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationInventoryFlowJpaRepository extends JpaRepository<LocationInventoryFlow, Long> {
    List<LocationInventoryFlow> findByWarehouseIdAndLocationId(Long warehouseId, Long locationId);
}
