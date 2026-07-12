package com.chaobo.scm.iam.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class UserAggregate {
    private final long id;
    private final String username;
    private String passwordHash;
    private int status;
    private int failedAttempts;
    private int version;

    public UserAggregate(long id, String username, String passwordHash, int status, int failedAttempts, int version) {
        if (username == null || username.isBlank() || passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "用户名和密码不能为空");
        }
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.version = version;
    }

    public void authenticate(String passwordHash) {
        if (status != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户不可登录");
        }
        if (!this.passwordHash.equals(passwordHash)) {
            failedAttempts++;
            if (failedAttempts >= 5) {
                status = 3;
            }
            version++;
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户名或密码错误");
        }
        failedAttempts = 0;
        version++;
    }

    public void disable() {
        if (status == 2) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "用户已停用");
        }
        status = 2;
        version++;
    }

    public void enable() {
        status = 1;
        failedAttempts = 0;
        version++;
    }

    public void resetPassword(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "新密码不能为空");
        }
        this.passwordHash = passwordHash;
        failedAttempts = 0;
        version++;
    }

    public long id() { return id; }
    public String username() { return username; }
    public String passwordHash() { return passwordHash; }
    public int status() { return status; }
    public int failedAttempts() { return failedAttempts; }
    public int version() { return version; }
}
