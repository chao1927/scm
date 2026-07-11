package com.chaobo.scm.purchase.domain.order;

public enum PurchaseOrderStatus {
    DRAFT(1, "草稿"),
    SUBMITTED(2, "待审批"),
    APPROVED(3, "已审批"),
    PENDING_SUPPLIER_CONFIRM(4, "待供应商确认"),
    SUPPLIER_CONFIRMED(5, "供应商已确认"),
    SUPPLIER_DIFF(6, "供应商差异"),
    PARTIALLY_INBOUNDED(7, "部分入库"),
    COMPLETED(8, "已完成"),
    CANCELLED(9, "已取消"),
    CLOSED(10, "已关闭"),
    REJECTED(11, "已驳回");

    private final int code;
    private final String label;

    PurchaseOrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static PurchaseOrderStatus of(int code) {
        for (PurchaseOrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知采购订单状态: " + code);
    }
}
