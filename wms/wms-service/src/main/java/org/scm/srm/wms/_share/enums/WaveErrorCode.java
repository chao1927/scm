package org.scm.srm.wms._share.enums;

import org.scm.common.exception.ErrorCode;

public enum WaveErrorCode implements ErrorCode {
    WAVE_NOT_FOUND("W001", "波次单不存在");

    private final String code;
    private final String message;

    WaveErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
