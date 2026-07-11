package com.chaobo.scm.purchase.domain.price;

public enum PurchasePriceStatus {
    ACTIVE(1, "启用"),
    DISABLED(2, "停用");

    private final int code;
    private final String label;

    PurchasePriceStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static PurchasePriceStatus of(int code) {
        for (PurchasePriceStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知采购价格状态: " + code);
    }
}
