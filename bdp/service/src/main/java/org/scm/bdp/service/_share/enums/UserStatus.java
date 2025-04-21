package org.scm.bdp.service._share.enums;

public enum UserStatus {
    // 状态：1启用，0禁用
    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final Integer code;
    private final String message;

    UserStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
