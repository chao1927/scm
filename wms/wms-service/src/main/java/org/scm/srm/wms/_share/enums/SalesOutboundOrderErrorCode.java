package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum SalesOutboundOrderErrorCode implements ErrorCode {
    NOT_FOUND("SO001", "销售出库单不存在"),
    ALREADY_COMPLETED("SO002", "销售出库单已完成");

    private final String code;
    private final String message;

    SalesOutboundOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
