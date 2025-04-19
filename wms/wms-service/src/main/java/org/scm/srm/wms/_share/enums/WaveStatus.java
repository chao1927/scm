package org.scm.srm.wms._share.enums;

public enum WaveStatus {
    CREATED(1, "已创建"),
    COMPLETED(2, "已完成");

    private final int code;
    private final String description;

    WaveStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
