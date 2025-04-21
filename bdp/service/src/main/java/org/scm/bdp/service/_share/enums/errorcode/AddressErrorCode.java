package org.scm.bdp.service._share.enums.errorcode;

import org.scm.common.exception.ErrorCode;

public enum AddressErrorCode implements ErrorCode {

    // 地址id错误
    ADDRESS_ID_ERROR("A001", "地址id错误"),
    // 地址不存在
    ADDRESS_NOT_EXIST("A002", "地址不存在"),
    // 地址已禁用
    ADDRESS_DISABLED("A003", "地址已禁用"),

    ;

    private final String code;
    private final String message;

    AddressErrorCode(String code, String message) {
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
