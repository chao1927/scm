package com.chaobo.scm.supplier.domain.profile;

public enum ProfileChangeStatus {
    PENDING(1, "待审批"), WITHDRAWN(2, "已撤回"), APPROVED(3, "已通过"), REJECTED(4, "已驳回");
    private final int code;
    private final String label;
    ProfileChangeStatus(int code, String label) { this.code = code; this.label = label; }
    public int code() { return code; }
    public String label() { return label; }
    public static ProfileChangeStatus fromCode(int code) {
        for (var value : values()) if (value.code == code) return value;
        throw new IllegalArgumentException("未知资料变更状态: " + code);
    }
}
