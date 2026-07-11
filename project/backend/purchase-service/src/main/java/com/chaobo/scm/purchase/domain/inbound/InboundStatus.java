package com.chaobo.scm.purchase.domain.inbound;

public enum InboundStatus {
    ASN_RECORDED(1, "ASN已记录"),
    IN_TRANSIT(2, "在途"),
    ARRIVED(3, "已到仓"),
    RECEIVED(4, "已收货"),
    INSPECTED(5, "已质检"),
    PUTAWAY(6, "已上架"),
    EXCEPTION(7, "异常");

    private final int code;
    private final String label;

    InboundStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() { return code; }
    public String label() { return label; }

    public static InboundStatus of(int code) {
        for (InboundStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知到货状态: " + code);
    }
}
