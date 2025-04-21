package org.scm.bdp.service._share.enums.errorcode;

import org.scm.common.exception.ErrorCode;

public enum UnitOfMeasureErrorCode implements ErrorCode {


    UNIT_OF_MEASURE_NOT_FOUND("UM10001", "unit of measure not found"),

    UNIT_OF_MEASURE_NAME_DUPLICATE("UM10002", "unit of measure name duplicate"),

    UNIT_OF_MEASURE_EXIST("UM10003", "unit of measure exist");

    private final String code;

    private final String message;

    UnitOfMeasureErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
