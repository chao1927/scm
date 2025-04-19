package org.scm.oms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum DeliveryOrderErrorCode implements ErrorCode {

    DELIVERY_ORDER_NOT_FOUND("D001", "发货单不存在"),
    INVALID_STATUS_TRANSITION("D002", "发货单状态变更非法");

    private final String code;
    private final String message;

    DeliveryOrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
