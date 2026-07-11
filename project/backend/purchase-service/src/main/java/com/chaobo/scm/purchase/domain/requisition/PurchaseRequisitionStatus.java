package com.chaobo.scm.purchase.domain.requisition;

public enum PurchaseRequisitionStatus {
    DRAFT(1, "草稿"),
    SUBMITTED(2, "审批中"),
    APPROVED(3, "已批准"),
    REJECTED(4, "已驳回"),
    PARTIALLY_CONVERTED(5, "部分转采购"),
    CONVERTED(6, "已转采购"),
    CLOSED(7, "已关闭");

    private final int code;
    private final String label;

    PurchaseRequisitionStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static PurchaseRequisitionStatus of(int code) {
        for (PurchaseRequisitionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知请购状态: " + code);
    }
}
