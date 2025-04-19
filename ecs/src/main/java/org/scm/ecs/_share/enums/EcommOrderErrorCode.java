package org.scm.ecs._share.enums;

import org.scm.common.exception.ErrorCode;

public enum EcommOrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND("EO001", "平台订单不存在");

    private final String code;
    private final String message;

    EcommOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
