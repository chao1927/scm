package com.chaobo.scm.wms.domain.receiving;

public enum ReceiptStatus {
    RECEIVING(1, "收货中"),
    COMPLETED(2, "已收货"),
    EXCEPTION(3, "异常");

    private final int code;
    private final String label;
    ReceiptStatus(int code, String label) { this.code = code; this.label = label; }
    public int code() { return code; }
    public String label() { return label; }
}
