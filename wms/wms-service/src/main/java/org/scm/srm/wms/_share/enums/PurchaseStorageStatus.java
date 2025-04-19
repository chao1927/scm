package org.scm.srm.wms._share.enums;

public enum PurchaseStorageStatus {
    TO_START(1, "待入库"),
    INSPECTING(2, "质检中"),
    SHELVING(3, "上架中"),
    COMPLETED(4, "已完成");

    private final int code;
    private final String description;

    PurchaseStorageStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }

    public String getDescription() { return description; }
}
