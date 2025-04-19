package org.scm.srm.wms.adapter.infra.repository;

import org.scm.srm.wms.domain.model.PurchaseStorageAgg;
import org.scm.srm.wms.domain.repository.PurchaseStorageRepository;

public class PurchaseStorageRepositoryImpl implements PurchaseStorageRepository {
    @Override
    public void save(PurchaseStorageAgg purchaseStorageAgg) {

    }

    @Override
    public PurchaseStorageAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public PurchaseStorageAgg findByStorageNo(String storageNo) {
        return null;
    }
}
