package org.scm.tms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum LogisticsOrderErrorCode implements ErrorCode {
    NOT_FOUND("L001", "物流单不存在");

    private final String code;
    private final String message;

    LogisticsOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
