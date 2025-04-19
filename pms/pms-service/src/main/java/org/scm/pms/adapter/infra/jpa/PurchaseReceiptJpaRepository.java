package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.PurchaseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseReceiptJpaRepository extends JpaRepository<PurchaseReceipt, Long> {
    PurchaseReceipt findByReceiptNo(String receiptNo);
}
