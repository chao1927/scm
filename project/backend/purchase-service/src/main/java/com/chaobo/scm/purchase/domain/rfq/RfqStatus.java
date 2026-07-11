package com.chaobo.scm.purchase.domain.rfq;

public enum RfqStatus {
    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    QUOTING(3, "报价中"),
    BIDDING_CLOSED(4, "已截标"),
    AWARDED(5, "已定标"),
    CANCELLED(6, "已取消"),
    CLOSED(7, "已关闭");

    private final int code;
    private final String label;

    RfqStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static RfqStatus of(int code) {
        for (RfqStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知询价状态: " + code);
    }
}
