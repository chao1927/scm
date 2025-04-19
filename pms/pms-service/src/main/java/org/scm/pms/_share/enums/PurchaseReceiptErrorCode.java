package org.scm.pms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum PurchaseReceiptErrorCode implements ErrorCode {
    RECEIPT_NOT_FOUND("R001", "收货单不存在"),
    INVALID_RECEIPT_STATUS("R002", "收货单状态非法");

    private final String code;
    private final String message;

    PurchaseReceiptErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
