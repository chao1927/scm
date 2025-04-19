package org.scm.common.exception;

public enum EmployeeErrorCode implements ErrorCode {
    EMPLOYEE_NOT_FOUND("E001", "员工不存在");


    private final String code;
    private final String message;

    EmployeeErrorCode(String code, String message) {
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
