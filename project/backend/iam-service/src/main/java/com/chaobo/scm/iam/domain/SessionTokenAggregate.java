package com.chaobo.scm.iam.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

public class SessionTokenAggregate {
    private final long id;
    private final long userId;
    private final String accessToken;
    private final String refreshToken;
    private int status;
    private int version;

    public SessionTokenAggregate(long id, long userId, String accessToken, String refreshToken, int status, int version) {
        if (userId <= 0 || accessToken == null || accessToken.isBlank() || refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "会话令牌不合法");
        }
        this.id = id;
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.status = status;
        this.version = version;
    }

    public void logout() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "会话已失效");
        }
        status = 2;
        version++;
    }

    public long id() { return id; }
    public long userId() { return userId; }
    public String accessToken() { return accessToken; }
    public String refreshToken() { return refreshToken; }
    public int status() { return status; }
    public int version() { return version; }
}
