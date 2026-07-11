package com.chaobo.scm.purchase.domain.orderchange;

public enum PurchaseOrderChangeStatus {
    PENDING_APPROVAL(1, "待审批"),
    EFFECTIVE(2, "已生效"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String label;

    PurchaseOrderChangeStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() { return code; }
    public String label() { return label; }

    public static PurchaseOrderChangeStatus of(int code) {
        for (PurchaseOrderChangeStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知采购订单变更状态: " + code);
    }
}
