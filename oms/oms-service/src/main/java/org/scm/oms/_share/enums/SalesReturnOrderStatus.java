package org.scm.oms._share.enums;

public enum SalesReturnOrderStatus {
    CREATED(1, "创建中"),
    WAITING_RECEIVE(2, "待收货"),
    INSPECTION(3, "验收中"),
    COMPLETED(4, "已完成"),
    REJECTED(5, "拒绝收货");

    private final int code;
    private final String description;

    SalesReturnOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
