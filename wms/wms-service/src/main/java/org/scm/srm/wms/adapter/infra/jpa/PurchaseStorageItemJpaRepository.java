package org.scm.srm.wms.adapter.infra.jpa;

import org.scm.srm.wms.adapter.infra.domain.PurchaseStorageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseStorageItemJpaRepository extends JpaRepository<PurchaseStorageItem, Long> {
    List<PurchaseStorageItem> findByStorageNo(String storageNo);
}
