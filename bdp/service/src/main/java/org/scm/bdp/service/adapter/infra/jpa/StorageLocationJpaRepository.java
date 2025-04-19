package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageLocationJpaRepository extends JpaRepository<StorageLocation, Long> {

    List<StorageLocation> findByWarehouseId(Long warehouseId);

}