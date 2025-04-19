package org.scm.pms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum ReturnSupplyDeliveryErrorCode implements ErrorCode {
    DELIVERY_NOT_FOUND("RSD001", "退供发货单不存在");

    private final String code;
    private final String message;

    ReturnSupplyDeliveryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
