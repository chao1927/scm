package org.scm.pms.domain.model;

import org.scm.common.exception.BizException;
import org.scm.common.exception.PurchaseErrorCode;
import org.scm.pms._share.enums.PurchaseOrderStatus;
import org.scm.pms.adapter.infra.domain.PurchaseOrder;
import org.scm.pms.application.command.CreatePurchaseOrderCommand;

public record PurchaseOrderAgg(PurchaseOrder purchaseOrder) {

    public static PurchaseOrderAgg create(CreatePurchaseOrderCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public Long id() {
        return purchaseOrder.getId();
    }

    private PurchaseOrderStatus getStatusEnum() {
        return PurchaseOrderStatus.of(purchaseOrder.getStatus());
    }

    public void submit() {
        if (!getStatusEnum().canSubmit()) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.SUBMITTED.getCode());
    }

    public void audit() {
        if (!getStatusEnum().canAudit()) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.AUDITED.getCode());
    }

    public void confirm() {
        if (!getStatusEnum().canConfirm()) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.CONFIRMED.getCode());
    }

    public void receive() {
        if (!getStatusEnum().canReceive()) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED.getCode());
    }

    public void complete() {
        if (!getStatusEnum().canComplete()) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.COMPLETED.getCode());
    }

    public void cancel() {
        if (!getStatusEnum().canCancel()) {
            throw new BizException(PurchaseErrorCode.INVALID_STATUS);
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED.getCode());
    }

}
