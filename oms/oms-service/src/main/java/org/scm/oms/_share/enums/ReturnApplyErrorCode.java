package org.scm.oms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum ReturnApplyErrorCode implements ErrorCode {
    RETURN_APPLY_NOT_FOUND("R001", "退货申请不存在");

    private final String code;
    private final String message;

    ReturnApplyErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
