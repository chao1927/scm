package org.scm.pms._share.enums;

public enum PurchaseOrderStatus {

    DRAFT(1, "待提交"),
    SUBMITTED(2, "审核中"),
    AUDITED(3, "待发货"),
    CONFIRMED(4, "待收货"),
    RECEIVED(5, "待打款"),
    COMPLETED(6, "已完成"),
    CANCELLED(7, "已取消");

    private final int code;
    private final String description;

    PurchaseOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PurchaseOrderStatus of(Integer status) {
        for (PurchaseOrderStatus statusEnum : values()) {
            if (statusEnum.getCode() == status) {
                return statusEnum;
            }
        }
        return null;
    }

    public int getCode() { return code; }

    public String getDescription() { return description; }

    // 可扩展状态校验逻辑
    public boolean canSubmit() { return this == DRAFT; }
    public boolean canAudit() { return this == SUBMITTED; }
    public boolean canConfirm() { return this == AUDITED; }
    public boolean canReceive() { return this == CONFIRMED; }
    public boolean canComplete() { return this == RECEIVED; }
    public boolean canCancel() { return this != COMPLETED && this != CANCELLED; }
}
