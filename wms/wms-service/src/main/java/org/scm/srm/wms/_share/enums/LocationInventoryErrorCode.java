package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum LocationInventoryErrorCode implements ErrorCode {
    INVENTORY_NOT_FOUND("LI001", "库位库存不存在");

    private final String code;
    private final String message;

    LocationInventoryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
