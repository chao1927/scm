package org.scm.bdp.service._share.enums.errorcode;

import org.scm.common.exception.ErrorCode;

public enum SupplierErrorCode implements ErrorCode {
    // "供应商分类名称已存在"
    SUPPLIER_CATEGORY_NAME_EXIST("S10001", "供应商分类名称已存在"),


    SUPPLIER_CATEGORY_NAME_DUPLICATE("S10002", "供应商分类名称重复"),

    SUPPLIER_CATEGORY_NOT_FOUND("S10003", "供应商分类不存在"),


    SUPPLIER_CATEGORY_EXIST_SUPPLIER("S10004", "供应商分类存在供应商"),

    SUPPLIER_NAME_EXIST("S10005", "供应商名称已存在"),

    SUPPLIER_NOT_FOUND("S10006", "供应商不存在");

    private final String code;

    private final String message;

    SupplierErrorCode(String code, String message) {
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
