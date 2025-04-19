package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum InventoryErrorCode implements ErrorCode {
    INSUFFICIENT_INVENTORY("INV001", "库存不足");

    private final String code;
    private final String message;

    InventoryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
