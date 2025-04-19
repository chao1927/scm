package org.scm.pms.domain.model;

import org.scm.pms._share.enums.PurchaseReceiptStatus;
import org.scm.pms.adapter.infra.domain.PurchaseReceipt;

public record PurchaseReceiptAgg(PurchaseReceipt receipt) {

    public void beginReceive() {
        this.receipt.setStatus(PurchaseReceiptStatus.TO_RECEIVE.getCode());
    }

    public void finishReceive() {
        this.receipt.setStatus(PurchaseReceiptStatus.ALL_RECEIVED.getCode());
    }

    public void forceComplete() {
        this.receipt.setStatus(PurchaseReceiptStatus.FORCE_COMPLETED.getCode());
    }

}
