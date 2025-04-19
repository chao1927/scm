package org.scm.common.exception;

public enum WarehouseErrorCode implements ErrorCode {

    WAREHOUSE_NOT_FOUND("W001", "仓库不存在"),
    DUPLICATE_WAREHOUSE_NAME("W002", "仓库名称重复"),
    DUPLICATE_WAREHOUSE_CODE("W003", "仓库编码重复"),
    DUPLICATE_WAREHOUSE_NO("W004", "仓库编号重复"),
    DUPLICATE_STORAGE_LOCATION_NAME("W005", "库位名称重复"),

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
