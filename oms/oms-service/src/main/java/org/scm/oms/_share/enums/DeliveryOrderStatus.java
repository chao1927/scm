package org.scm.oms._share.enums;

public enum DeliveryOrderStatus {

    PENDING_WAVE(1, "待分波"),
    PENDING_SORT(2, "待分拣"),
    PENDING_REVIEW(3, "待复核"),
    PENDING_PACK(4, "待打包"),
    PENDING_WEIGHT(5, "待称重"),
    PENDING_ASSIGN_LOGISTICS(6, "待分配物流"),
    PENDING_OUTBOUND(7, "待出库"),
    PENDING_PICKUP(8, "待揽收"),
    DELIVERED(9, "发货成功");

    private final int code;
    private final String description;

    DeliveryOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }

    public String getDescription() { return description; }
}
