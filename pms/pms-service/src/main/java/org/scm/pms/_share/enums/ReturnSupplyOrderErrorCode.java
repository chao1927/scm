package org.scm.pms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum ReturnSupplyOrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND("RSO001", "退供单不存在");

    private final String code;
    private final String message;

    ReturnSupplyOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
