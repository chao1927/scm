package com.chaobo.scm.iam.domain.mfa;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

import java.time.Instant;

/** MFA challenge aggregate; secrets are always represented as ciphertext. */
public final class MfaChallengeAggregate {

    private final long id;
    private final String challengeNo;
    private final long userId;
    private final String appCode;
    private final long sessionId;
    private final String purpose;
    private final String deviceDigest;
    private final String secretCiphertext;
    private final String idempotencyKey;
    private final int maxAttempts;
    private final Instant expiresAt;
    private int failedAttempts;
    private Status status;
    private Instant verifiedAt;
    private int version;

    private MfaChallengeAggregate(long id, String challengeNo, long userId, String appCode,
                                  long sessionId, String purpose, String deviceDigest,
                                  String secretCiphertext, String idempotencyKey, int failedAttempts,
                                  int maxAttempts, Instant expiresAt, Status status,
                                  Instant verifiedAt, int version) {
        this.id = id;
        this.challengeNo = challengeNo;
        this.userId = userId;
        this.appCode = appCode;
        this.sessionId = sessionId;
        this.purpose = purpose;
        this.deviceDigest = deviceDigest;
        this.secretCiphertext = secretCiphertext;
        this.idempotencyKey = idempotencyKey;
        this.failedAttempts = failedAttempts;
        this.maxAttempts = maxAttempts;
        this.expiresAt = expiresAt;
        this.status = status;
        this.verifiedAt = verifiedAt;
        this.version = version;
    }

    public static MfaChallengeAggregate create(long id, String challengeNo, long userId,
                                               String appCode, long sessionId, String purpose,
                                               String deviceDigest, String secretCiphertext,
                                               String idempotencyKey, int maxAttempts,
                                               Instant expiresAt) {
        if (id <= 0 || userId <= 0 || sessionId <= 0 || blank(challengeNo) || blank(appCode)
                || blank(purpose) || blank(deviceDigest)
                || blank(secretCiphertext) || blank(idempotencyKey)
                || maxAttempts <= 0 || expiresAt == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "MFA挑战参数不合法");
        }
        return new MfaChallengeAggregate(id, challengeNo, userId, appCode, sessionId, purpose,
                deviceDigest, secretCiphertext,
                idempotencyKey, 0, maxAttempts, expiresAt, Status.PENDING, null, 0);
    }

    public static MfaChallengeAggregate restore(long id, String challengeNo, long userId,
                                                String appCode, long sessionId, String purpose,
                                                String deviceDigest, String secretCiphertext,
                                                String idempotencyKey, int failedAttempts,
                                                int maxAttempts, Instant expiresAt, Status status,
                                                Instant verifiedAt, int version) {
        return new MfaChallengeAggregate(id, challengeNo, userId, appCode, sessionId, purpose,
                deviceDigest, secretCiphertext,
                idempotencyKey, failedAttempts, maxAttempts, expiresAt, status, verifiedAt, version);
    }

    public void assertContext(long requestedSessionId, String requestedPurpose, String requestedDeviceDigest) {
        if (sessionId != requestedSessionId || !purpose.equals(requestedPurpose)
                || !deviceDigest.equals(requestedDeviceDigest)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "MFA挑战上下文不匹配");
        }
    }

    public boolean canVerify(Instant now) {
        if (status != Status.PENDING) {
            return false;
        }
        if (!now.isBefore(expiresAt)) {
            status = Status.EXPIRED;
            version++;
            return false;
        }
        return true;
    }

    public void recordFailure(Instant now) {
        if (!canVerify(now)) {
            return;
        }
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            status = Status.LOCKED;
        }
        version++;
    }

    public void verify(Instant now) {
        if (!canVerify(now)) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "MFA挑战不可验证");
        }
        status = Status.VERIFIED;
        verifiedAt = now;
        version++;
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    public long id() { return id; }
    public String challengeNo() { return challengeNo; }
    public long userId() { return userId; }
    public String appCode() { return appCode; }
    public long sessionId() { return sessionId; }
    public String purpose() { return purpose; }
    public String deviceDigest() { return deviceDigest; }
    public String secretCiphertext() { return secretCiphertext; }
    public String idempotencyKey() { return idempotencyKey; }
    public int failedAttempts() { return failedAttempts; }
    public int maxAttempts() { return maxAttempts; }
    public Instant expiresAt() { return expiresAt; }
    public Status status() { return status; }
    public Instant verifiedAt() { return verifiedAt; }
    public int version() { return version; }

    public enum Status { PENDING, VERIFIED, LOCKED, EXPIRED }
}
