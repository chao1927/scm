package org.scm.bdp.service._share.enums.errorcode;

import lombok.Getter;
import lombok.ToString;
import org.scm.common.exception.ErrorCode;

@Getter
@ToString
public enum PermissionErrorCode implements ErrorCode {

    PERMISSION_METHOD_NOT_ALLOWED("P10001", "Permission method not allowed"),

    // 权限名称重复
    PERMISSION_NAME_DUPLICATE("P10002", "Permission name duplicate"),


    PERMISSION_CODE_DUPLICATE("P10003", "Permission code duplicate"),


    PERMISSION_NOT_FOUND("P10004", "Permission not found"),


    PERMISSION_BEEN_USED("P10005", "Permission been used"),

    // 权限名称已经存在
    PERMISSION_NAME_EXIST("P10006", "Permission name exist"),

    // 权限 code 已经存在
    PERMISSION_CODE_EXIST("P10007", "Permission code exist"),


    ;

    private final String code;

    private final String message;

    PermissionErrorCode(String code, String message) {
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
