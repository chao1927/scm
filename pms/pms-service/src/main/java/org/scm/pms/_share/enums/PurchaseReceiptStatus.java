package org.scm.pms._share.enums;

public enum PurchaseReceiptStatus {
    TO_RECEIVE(1, "待收货"),
    PARTIAL_RECEIVED(2, "部分收货"),
    ALL_RECEIVED(3, "全部收货"),
    FORCE_COMPLETED(4, "缺货收货");

    private final int code;
    private final String description;

    PurchaseReceiptStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
