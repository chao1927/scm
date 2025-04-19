package org.scm.oms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum OmsOrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND("O001", "订单不存在");

    private final String code;
    private final String message;

    OmsOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
