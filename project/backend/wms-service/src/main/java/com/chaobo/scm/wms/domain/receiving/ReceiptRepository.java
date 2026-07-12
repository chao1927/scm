package com.chaobo.scm.wms.domain.receiving;

import java.util.Optional;

public interface ReceiptRepository {
    Optional<ReceiptAggregate> findByNo(String receiptNo);
    void save(ReceiptAggregate aggregate, long operatorId);
}
