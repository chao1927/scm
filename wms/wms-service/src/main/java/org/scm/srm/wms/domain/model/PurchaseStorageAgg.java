package org.scm.srm.wms.domain.model;


import org.scm.srm.wms.adapter.infra.domain.PurchaseStorage;

public record PurchaseStorageAgg(PurchaseStorage purchaseStorage) {


    public void start(Long empId) {
        // TODO 待入库
    }

    public void inspect() {
        // TODO 质检中
    }

    public void shelf() {
        // TODO 上架中
    }

}
