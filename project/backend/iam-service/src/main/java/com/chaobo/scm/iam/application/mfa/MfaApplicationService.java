package com.chaobo.scm.iam.application.mfa;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.application.TokenCachePort;
import com.chaobo.scm.iam.domain.mfa.MfaChallengeAggregate;
import com.chaobo.scm.iam.domain.mfa.MfaVerificationPolicy;
import com.chaobo.scm.iam.infrastructure.persistence.MfaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** MFA enrollment, bound challenge, recovery-code and administrative reset use cases. */
@Service
public class MfaApplicationService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int RECOVERY_CODE_COUNT = 8;
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final MfaMapper mapper;
    private final MfaVerificationPolicy.SecretProtector secretProtector;
    private final MfaVerificationPolicy.TotpVerifier totpVerifier;
    private final TokenCachePort tokenCache;
    private final Clock clock;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    @Autowired
    public MfaApplicationService(MfaMapper mapper,
                                 MfaVerificationPolicy.SecretProtector secretProtector,
                                 MfaVerificationPolicy.TotpVerifier totpVerifier,
                                 TokenCachePort tokenCache) {
        this(mapper, secretProtector, totpVerifier, tokenCache, Clock.systemUTC());
    }

    MfaApplicationService(MfaMapper mapper,
                          MfaVerificationPolicy.SecretProtector secretProtector,
                          MfaVerificationPolicy.TotpVerifier totpVerifier,
                          TokenCachePort tokenCache,
                          Clock clock) {
        this.mapper = mapper;
        this.secretProtector = secretProtector;
        this.totpVerifier = totpVerifier;
        this.tokenCache = tokenCache;
        this.clock = clock;
    }

    @Transactional(rollbackFor = Exception.class)
    public EnrollmentView enroll(EnrollmentCommand command) {
        if (command == null || command.userId() <= 0 || blank(command.totpSecret())) {
            throw invalid("MFA注册请求不合法");
        }
        mapper.upsertConfiguration(new MfaMapper.ConfigurationRow(ids.incrementAndGet(), command.userId(),
                secretProtector.encrypt(command.totpSecret()), 0, 0));
        audit(command.userId(), "MFA_ENROLLMENT_STARTED", null, command.userId(), "pending confirmation");
        return new EnrollmentView(command.userId(), "PENDING_CONFIRMATION");
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = BusinessException.class)
    public EnrollmentView confirmEnrollment(long userId, String code) {
        MfaMapper.ConfigurationRow config = mapper.findConfiguration(userId);
        if (config == null || config.status() != 0
                || !totpVerifier.verify(secretProtector.decrypt(config.secretCiphertext()), code, clock.instant())) {
            audit(userId, "MFA_ENROLLMENT_CONFIRM_FAILED", null, userId, "invalid confirmation code");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "MFA注册验证码无效");
        }
        mapper.updateConfigurationStatus(userId, 1);
        audit(userId, "MFA_ENROLLED", null, userId, "enrollment confirmed");
        outbox("MfaEnrolled", userId, "userId=" + userId);
        return new EnrollmentView(userId, "ACTIVE");
    }

    @Transactional(rollbackFor = Exception.class)
    public ChallengeView create(CreateCommand command) {
        validateCreate(command);
        MfaMapper.ChallengeRow existing = mapper.findByIdempotencyKey(command.idempotencyKey());
        if (existing != null) {
            if (existing.userId() != command.userId() || existing.sessionId() != command.sessionId()
                    || !existing.purpose().equals(command.purpose())) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "MFA幂等键已被其他上下文使用");
            }
            return view(existing, true);
        }
        MfaMapper.ConfigurationRow config = mapper.findActiveConfiguration(command.userId());
        if (config == null) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "用户未启用MFA");
        }
        long id = ids.incrementAndGet();
        Instant expiresAt = clock.instant().plus(CHALLENGE_TTL);
        String challengeNo = "MFA" + id;
        MfaChallengeAggregate challenge = MfaChallengeAggregate.create(id, challengeNo,
                command.userId(), command.appCode(), command.sessionId(), command.purpose(),
                command.deviceDigest(), config.secretCiphertext(), command.idempotencyKey(),
                MAX_ATTEMPTS, expiresAt);
        mapper.insertChallenge(toRow(challenge));
        audit(command.userId(), "MFA_CHALLENGE_CREATED", challengeNo, command.userId(), command.purpose());
        outbox("MfaChallengeCreated", command.userId(), "challengeNo=" + challengeNo);
        return view(toRow(challenge), false);
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = BusinessException.class)
    public VerificationResult verify(String challengeNo, VerifyCommand command) {
        if (command == null || blank(command.code())) { throw invalid("MFA验证请求不合法"); }
        MfaMapper.ChallengeRow row = required(challengeNo);
        MfaChallengeAggregate challenge = restore(row);
        challenge.assertContext(command.sessionId(), command.purpose(), command.deviceDigest());
        Instant now = clock.instant();
        int oldVersion = challenge.version();
        if (!challenge.canVerify(now)) {
            persist(challenge, oldVersion);
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "MFA挑战已过期、锁定或已使用");
        }
        boolean valid = switch (command.method()) {
            case TOTP -> totpVerifier.verify(secretProtector.decrypt(challenge.secretCiphertext()), command.code(), now);
            case RECOVERY_CODE -> mapper.consumeRecoveryCode(challenge.userId(),
                    MfaVerificationPolicy.recoveryCodeHash(command.code()), challenge.challengeNo()) == 1;
        };
        if (!valid) {
            challenge.recordFailure(now);
            persist(challenge, oldVersion);
            String action = challenge.status() == MfaChallengeAggregate.Status.LOCKED
                    ? "MFA_CHALLENGE_LOCKED" : "MFA_CHALLENGE_FAILED";
            audit(challenge.userId(), action, challengeNo, challenge.userId(), "invalid verification code");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "MFA验证码无效");
        }
        challenge.verify(now);
        persist(challenge, oldVersion);
        audit(challenge.userId(), "MFA_CHALLENGE_VERIFIED", challengeNo, challenge.userId(), challenge.purpose());
        outbox("MfaChallengeVerified", challenge.userId(), "challengeNo=" + challengeNo);
        return new VerificationResult(challenge.challengeNo(), true, challenge.status().name(), challenge.verifiedAt());
    }

    @Transactional(rollbackFor = Exception.class)
    public RecoveryCodesView regenerateRecoveryCodes(long userId, String verifiedChallengeNo) {
        MfaMapper.ChallengeRow challenge = required(verifiedChallengeNo);
        if (challenge.userId() != userId || !"VERIFIED".equals(challenge.challengeStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "必须使用已验证的本人MFA挑战");
        }
        mapper.invalidateRecoveryCodes(userId, "REGENERATED");
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            String code = recoveryCode();
            codes.add(code);
            mapper.insertRecoveryCode(ids.incrementAndGet(), userId, MfaVerificationPolicy.recoveryCodeHash(code));
        }
        audit(userId, "MFA_RECOVERY_CODES_REGENERATED", verifiedChallengeNo, userId, "count=" + codes.size());
        return new RecoveryCodesView(userId, List.copyOf(codes));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResetResult reset(ResetCommand command) {
        if (command == null || command.userId() <= 0 || command.operatorId() <= 0 || blank(command.reason())
                || blank(command.approvalReference()) || !command.highRiskVerified()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "MFA重置需要高风险验证、原因和审批依据");
        }
        mapper.updateConfigurationStatus(command.userId(), 2);
        mapper.invalidateRecoveryCodes(command.userId(), "ADMIN_RESET");
        List<Long> sessionIds = mapper.findActiveSessionIds(command.userId());
        mapper.revokeSessions(command.userId(), "MFA_RESET");
        sessionIds.forEach(tokenCache::revoke);
        audit(command.userId(), "MFA_RESET", null, command.operatorId(),
                command.reason() + ";approval=" + command.approvalReference());
        outbox("MfaReset", command.userId(), "operatorId=" + command.operatorId());
        return new ResetResult(command.userId(), sessionIds.size(), "DISABLED");
    }

    public ChallengeView get(String challengeNo) { return view(required(challengeNo), false); }

    private void persist(MfaChallengeAggregate challenge, int oldVersion) {
        if (mapper.updateChallenge(challenge.id(), challenge.status().name(), challenge.failedAttempts(),
                challenge.verifiedAt(), challenge.version(), oldVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "MFA挑战版本冲突");
        }
    }

    private MfaMapper.ChallengeRow required(String challengeNo) {
        MfaMapper.ChallengeRow row = mapper.findByChallengeNo(challengeNo);
        if (row == null) { throw new BusinessException(ErrorCode.NOT_FOUND, "MFA挑战不存在"); }
        return row;
    }

    private void audit(long userId, String action, String challengeNo, Long operatorId, String reason) {
        mapper.insertAudit(new MfaMapper.AuditRow(ids.incrementAndGet(), userId, action, challengeNo,
                operatorId, reason, clock.instant()));
    }

    private void outbox(String type, long userId, String payload) {
        mapper.insertOutbox(new MfaMapper.OutboxRow(ids.incrementAndGet(), type, Long.toString(userId),
                payload, clock.instant()));
    }

    private static void validateCreate(CreateCommand command) {
        if (command == null || command.userId() <= 0 || command.sessionId() <= 0 || blank(command.appCode())
                || blank(command.purpose()) || blank(command.deviceDigest()) || blank(command.idempotencyKey())
                || command.idempotencyKey().length() > 128) {
            throw invalid("MFA挑战请求不合法");
        }
    }

    private static MfaChallengeAggregate restore(MfaMapper.ChallengeRow row) {
        return MfaChallengeAggregate.restore(row.id(), row.challengeNo(), row.userId(), row.appCode(),
                row.sessionId(), row.purpose(), row.deviceDigest(), row.secretCiphertext(), row.idempotencyKey(),
                row.failedAttempts(), row.maxAttempts(), row.expiresAt(),
                MfaChallengeAggregate.Status.valueOf(row.challengeStatus()), row.verifiedAt(), row.version());
    }

    private static MfaMapper.ChallengeRow toRow(MfaChallengeAggregate value) {
        return new MfaMapper.ChallengeRow(value.id(), value.challengeNo(), value.userId(), value.appCode(),
                value.sessionId(), value.purpose(), value.deviceDigest(), "TOTP", value.secretCiphertext(),
                value.status().name(), value.failedAttempts(), value.maxAttempts(), value.expiresAt(),
                value.verifiedAt(), value.idempotencyKey(), value.version());
    }

    private static ChallengeView view(MfaMapper.ChallengeRow row, boolean idempotentHit) {
        return new ChallengeView(row.challengeNo(), row.userId(), row.appCode(), row.sessionId(), row.purpose(),
                row.challengeStatus(), row.failedAttempts(), row.maxAttempts(), row.expiresAt(),
                row.verifiedAt(), row.version(), idempotentHit);
    }

    private static String recoveryCode() {
        byte[] bytes = new byte[9];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toUpperCase();
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    public record EnrollmentCommand(long userId, String totpSecret) { }
    public record EnrollmentView(long userId, String status) { }
    public record CreateCommand(long userId, String appCode, long sessionId, String purpose,
                                String deviceDigest, String idempotencyKey) { }
    public record VerifyCommand(VerificationMethod method, String code, long sessionId,
                                String purpose, String deviceDigest) { }
    public enum VerificationMethod { TOTP, RECOVERY_CODE }
    public record ChallengeView(String challengeNo, long userId, String appCode, long sessionId,
                                String purpose, String status, int failedAttempts, int maxAttempts,
                                Instant expiresAt, Instant verifiedAt, int version, boolean idempotentHit) { }
    public record VerificationResult(String challengeNo, boolean verified, String status, Instant verifiedAt) { }
    public record RecoveryCodesView(long userId, List<String> recoveryCodes) { }
    public record ResetCommand(long userId, long operatorId, String reason, String approvalReference,
                               boolean highRiskVerified) { }
    public record ResetResult(long userId, int revokedSessionCount, String status) { }
}
