package org.scm.bdp.service.adapter.infra.jpa;

import org.scm.bdp.service.adapter.infra.domain.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageLocationJpaRepository extends JpaRepository<StorageLocation, Long> {

    Optional<List<StorageLocation>> findByWarehouseId(Long warehouseId);

    Optional<StorageLocation> findByCode(String code);

    Optional<StorageLocation> findByIdNotAndCode(Long id, String code);
}