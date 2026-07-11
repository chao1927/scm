package com.chaobo.scm.purchase.domain.supplierreturn;

public enum SupplierReturnStatus {
    CREATED(1, "已创建"),
    SUBMITTED(2, "待审批"),
    APPROVED(3, "已批准"),
    EXECUTION_NOTIFIED(4, "已通知执行"),
    CLOSED(5, "已关闭"),
    REJECTED(6, "已驳回");

    private final int code;
    private final String label;

    SupplierReturnStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() { return code; }
    public String label() { return label; }

    public static SupplierReturnStatus of(int code) {
        for (SupplierReturnStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知退供状态: " + code);
    }
}
