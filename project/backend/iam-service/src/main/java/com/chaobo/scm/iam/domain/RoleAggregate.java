package com.chaobo.scm.iam.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class RoleAggregate {
    private final long id;
    private final String roleCode;
    private String roleName;
    private int status;
    private int version;

    public RoleAggregate(long id, String roleCode, String roleName, int status, int version) {
        if (roleCode == null || roleCode.isBlank() || roleName == null || roleName.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "角色编码和名称不能为空");
        }
        this.id = id;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.status = status;
        this.version = version;
    }

    public void rename(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "角色名称不能为空");
        }
        this.roleName = roleName;
        version++;
    }

    public void disable() {
        status = 2;
        version++;
    }

    public long id() { return id; }
    public String roleCode() { return roleCode; }
    public String roleName() { return roleName; }
    public int status() { return status; }
    public int version() { return version; }
}
