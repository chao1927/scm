package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.PurchaseStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseStorageJpaRepository extends JpaRepository<PurchaseStorage, Long> {
    PurchaseStorage findByStorageNo(String storageNo);
}
