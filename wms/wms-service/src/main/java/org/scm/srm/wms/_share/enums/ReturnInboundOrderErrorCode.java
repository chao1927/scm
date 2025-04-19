package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum ReturnInboundOrderErrorCode implements ErrorCode {
    NOT_FOUND("R001", "退货入库单不存在");

    private final String code;
    private final String message;

    ReturnInboundOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
