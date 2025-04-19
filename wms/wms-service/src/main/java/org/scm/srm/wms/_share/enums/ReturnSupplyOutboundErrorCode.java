package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum ReturnSupplyOutboundErrorCode implements ErrorCode {
    OUTBOUND_NOT_FOUND("RSO001", "退供出库单不存在");

    private final String code;
    private final String message;

    ReturnSupplyOutboundErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
