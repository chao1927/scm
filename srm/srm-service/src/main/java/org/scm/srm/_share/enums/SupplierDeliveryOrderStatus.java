package org.scm.srm._share.enums;

public enum SupplierDeliveryOrderStatus {
    PENDING(1, "待发货"),
    IN_TRANSIT(2, "在途"),
    DELIVERED(3, "已送达");

    private final int code;
    private final String description;

    SupplierDeliveryOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
