package com.chaobo.scm.purchase.domain.comparison;

public enum BidComparisonStatus {
    GENERATED(1, "已生成"),
    AWARDED(2, "已定标"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String label;

    BidComparisonStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static BidComparisonStatus of(int code) {
        for (BidComparisonStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知比价状态: " + code);
    }
}
