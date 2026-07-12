package com.chaobo.scm.wms.domain.inbound;

public enum InboundOrderStatus {
    PENDING_ARRIVAL(1, "待到货"),
    ARRIVED(2, "已到货"),
    RECEIVING(3, "收货中"),
    RECEIVED(4, "待质检"),
    CANCELLED(9, "已取消");

    private final int code;
    private final String label;

    InboundOrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() { return code; }
    public String label() { return label; }

    public static InboundOrderStatus of(int code) {
        for (var value : values()) {
            if (value.code == code) return value;
        }
        throw new IllegalArgumentException("未知入库单状态: " + code);
    }
}
