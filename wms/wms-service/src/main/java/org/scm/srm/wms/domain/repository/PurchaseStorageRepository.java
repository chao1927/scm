package org.scm.srm.wms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.srm.wms.domain.model.PurchaseStorageAgg;

public interface PurchaseStorageRepository extends BaseRepository<PurchaseStorageAgg> {
    PurchaseStorageAgg findByStorageNo(String storageNo);
}
