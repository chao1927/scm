package org.scm.srm.wms._share.enums;

public enum ReturnInboundStatus {
    RECEIVING(1, "收货中"),
    INSPECTING(2, "验收中"),
    SHELVING(3, "上架中"),
    COMPLETED(4, "已完成");

    private final int code;
    private final String description;

    ReturnInboundStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
