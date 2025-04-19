package org.scm.pms.adapter.infra.repository;

import org.scm.pms.domain.model.PurchaseReceiptAgg;
import org.scm.pms.domain.repository.PurchaseReceiptRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseReceiptRepositoryImpl implements PurchaseReceiptRepository {


    @Override
    public void save(PurchaseReceiptAgg purchaseReceiptAgg) {

    }

    @Override
    public PurchaseReceiptAgg findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public PurchaseReceiptAgg findByReceiptNo(String receiptNo) {
        return null;
    }
}
