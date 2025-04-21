package org.scm.bdp.service._share.enums.errorcode;

import lombok.Getter;
import lombok.ToString;
import org.scm.common.exception.ErrorCode;

@ToString
@Getter
public enum UserErrorCode implements ErrorCode {


    USER_NOT_FOUND("U10001", "user not found"),

    // 用户手机号重复
    USER_PHONE_DUPLICATE("U10002", "user phone duplicate"),

    // 用户名称重复
    USER_NAME_DUPLICATE("U10003", "user name duplicate"),


    USER_NAME_EXIST("U10004", "user name exist"),

    USER_PHONE_EXIST("U10005", "user phone exist"),

    USER_PASSWORD_ERROR("U10006", "user password error");
    private final String code;

    private final String message;

    UserErrorCode(String code, String message) {
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
