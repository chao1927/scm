package org.scm.pms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum ReturnSupplyErrorCode implements ErrorCode {
    APPLY_NOT_FOUND("RS001", "退供申请不存在");

    private final String code;
    private final String message;

    ReturnSupplyErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
