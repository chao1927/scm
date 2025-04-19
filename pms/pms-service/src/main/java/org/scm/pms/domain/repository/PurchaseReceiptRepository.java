package org.scm.pms.domain.repository;

import org.scm.common.BaseRepository;
import org.scm.pms.domain.model.PurchaseReceiptAgg;

public interface PurchaseReceiptRepository extends BaseRepository<PurchaseReceiptAgg> {
    PurchaseReceiptAgg findByReceiptNo(String receiptNo);
}