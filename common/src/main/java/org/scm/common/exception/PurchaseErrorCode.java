package org.scm.common.exception;

public enum PurchaseErrorCode implements ErrorCode {
    INVALID_STATUS("P001", "状态不合法");

    private final String code;
    private final String message;

    PurchaseErrorCode(String code, String message) {
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
