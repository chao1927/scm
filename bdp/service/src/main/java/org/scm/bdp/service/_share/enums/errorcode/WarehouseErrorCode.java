package org.scm.bdp.service._share.enums.errorcode;

import org.scm.common.exception.ErrorCode;

public enum WarehouseErrorCode implements ErrorCode {

    WAREHOUSE_NOT_FOUND("W001", "仓库不存在"),
    DUPLICATE_WAREHOUSE_NAME("W002", "仓库名称重复"),
    DUPLICATE_WAREHOUSE_CODE("W003", "仓库编码重复"),
    DUPLICATE_WAREHOUSE_NO("W004", "仓库编号重复"),
    DUPLICATE_STORAGE_LOCATION_NAME("W005", "库位名称重复"),

    WAREHOUSE_NAME_EXIST("W006", "仓库名称已存在"),


    // "仓库类型不存在"
    WAREHOUSE_TYPE_NOT_EXIST("W007", "仓库类型不存在"),

    WAREHOUSE_NAME_DUPLICATE("W008", "仓库名称重复"),

    STORAGE_LOCATION_NOT_FOUND("W009", "库位不存在"),

    STORAGE_LOCATION_CODE_DUPLICATE("W010", "库位编码重复"),

    MIXING_STRATEGY_NOT_EXIST("W011", "混料策略不存在")
    ;
    private final String code;
    private final String message;

    WarehouseErrorCode(String code, String message) {
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
