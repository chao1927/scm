package org.scm.pms._share.enums;

public enum PurchaseApplyStatus {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    AUDITED(2, "已审核"),
    REJECTED(3, "已驳回"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String description;

    PurchaseApplyStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public static PurchaseApplyStatus of(int code) {
        for (PurchaseApplyStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid code for PurchaseApplyStatus: " + code);
    }
}
