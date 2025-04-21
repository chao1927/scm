package org.scm.bdp.service._share.enums.errorcode;

import lombok.Getter;
import org.scm.common.exception.ErrorCode;

@Getter
public enum RoleErrorCode implements ErrorCode {


    ROLE_NAME_DUPLICATE("R10001", "Role name duplicate"),

    ROLE_CODE_DUPLICATE("R10002", "Role code duplicate"),

    ROLE_NOT_FOUND("R10003", "Role not found"),

    // 角色名称已经存在
    ROLE_NAME_EXIST("R10004", "Role name exist"),


    // 角色code已经存在
    ROLE_CODE_EXIST("R10005", "Role code exist"),

    ROLE_BEEN_USED("R10006", "Role been used");


    private final String code;

    private final String message;

    RoleErrorCode(String code, String message) {
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
