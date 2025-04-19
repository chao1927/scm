package org.scm.pms.adapter.infra.jpa;

import org.scm.pms.adapter.infra.domain.PurchaseReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseReceiptItemJpaRepository extends JpaRepository<PurchaseReceiptItem, Long> {
    List<PurchaseReceiptItem> findByReceiptNo(String receiptNo);
}
