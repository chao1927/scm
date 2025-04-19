package org.scm.srm._share.enums;

import org.scm.common.exception.ErrorCode;

public enum DeliveryErrorCode implements ErrorCode {
    INVALID_STATUS("D001", "当前状态不允许此操作");

    private final String code;
    private final String message;

    DeliveryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
