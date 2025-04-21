package org.scm.bdp.service._share.enums.errorcode;

import lombok.Getter;
import lombok.ToString;
import org.scm.common.exception.ErrorCode;


@Getter
@ToString
public enum LogisticsChannelErrorCode implements ErrorCode {
    LOGISTICS_CHANNEL_NOT_FOUND("L10001", "物流渠道不存在");


    private final String code;

    private final String message;

    LogisticsChannelErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
