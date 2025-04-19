package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum SortingErrorCode implements ErrorCode {
    SORTING_ORDER_NOT_FOUND("S001", "分拣单不存在");

    private final String code;
    private final String message;

    SortingErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
