package org.scm.srm.wms._share.enums;

public enum SalesOutboundOrderStatus {
    PENDING(1, "待出库"),
    COMPLETED(2, "已出库");

    private final int code;
    private final String description;

    SalesOutboundOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }

    public String getDescription() { return description; }

    public static SalesOutboundOrderStatus of(int code) {
        for (SalesOutboundOrderStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Unknown status: " + code);
    }
}
