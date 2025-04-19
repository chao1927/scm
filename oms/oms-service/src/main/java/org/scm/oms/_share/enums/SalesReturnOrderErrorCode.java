package org.scm.oms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum SalesReturnOrderErrorCode implements ErrorCode {
    NOT_FOUND("S001", "销售退货单不存在");

    private final String code;
    private final String message;

    SalesReturnOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
