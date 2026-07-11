package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.util.Arrays;

public enum AsnStatus {
    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    APPOINTED(3, "已预约"),
    SHIPPED(4, "已发货"),
    ARRIVED(5, "已到仓"),
    RECEIVED(6, "已收货"),
    CANCELLED(7, "已取消"),
    CLOSED(8, "已关闭");

    private final int code;
    private final String label;

    AsnStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static AsnStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(value -> value.code == code)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, "未知 ASN 状态: " + code));
    }
}
