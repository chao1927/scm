package org.scm.oms._share.enums;

public enum ReturnApplyStatus {
    PENDING(1, "待审核"),
    APPROVED(2, "审核通过"),
    REJECTED(3, "审核拒绝");

    private final int code;
    private final String description;

    ReturnApplyStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
}
