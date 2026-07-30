package com.chaobo.scm.iam.application.mfa;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.iam.domain.mfa.MfaChallengeAggregate;
import com.chaobo.scm.iam.domain.mfa.MfaVerificationPolicy;
import com.chaobo.scm.iam.infrastructure.persistence.MfaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/** Independent MFA challenge use cases; login integration is intentionally outside this slice. */
@Service
public class MfaApplicationService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);
    private final MfaMapper mapper;
    private final MfaVerificationPolicy.SecretProtector secretProtector;
    private final MfaVerificationPolicy.TotpVerifier totpVerifier;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public MfaApplicationService(MfaMapper mapper,
                                 MfaVerificationPolicy.SecretProtector secretProtector,
                                 MfaVerificationPolicy.TotpVerifier totpVerifier) {
        this.mapper = mapper;
        this.secretProtector = secretProtector;
        this.totpVerifier = totpVerifier;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChallengeView create(CreateCommand command) {
        validateCreate(command);
        MfaMapper.ChallengeRow existing = mapper.findByIdempotencyKey(command.idempotencyKey());
        if (existing != null) {
            if (existing.userId() != command.userId() || !existing.appCode().equals(command.appCode())) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "MFA幂等键已被其他请求使用");
            }
            return view(existing, true);
        }
        long id = ids.incrementAndGet();
        Instant expiresAt = Instant.now().plus(CHALLENGE_TTL);
        String challengeNo = "MFA" + id;
        MfaChallengeAggregate challenge = MfaChallengeAggregate.create(id, challengeNo,
                command.userId(), command.appCode(), secretProtector.encrypt(command.totpSecret()),
                command.idempotencyKey(), MAX_ATTEMPTS, expiresAt);
        mapper.insertChallenge(toRow(challenge));
        return view(toRow(challenge), false);
    }

    @Transactional(rollbackFor = Exception.class)
    public VerificationResult verify(String challengeNo, VerifyCommand command) {
        MfaMapper.ChallengeRow row = required(challengeNo);
        MfaChallengeAggregate challenge = restore(row);
        Instant now = Instant.now();
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
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "MFA验证码无效");
        }
        challenge.verify(now);
        persist(challenge, oldVersion);
        return new VerificationResult(challenge.challengeNo(), true, challenge.status().name(), challenge.verifiedAt());
    }

    public ChallengeView get(String challengeNo) {
        return view(required(challengeNo), false);
    }

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

    private static void validateCreate(CreateCommand command) {
        if (command == null || command.userId() <= 0 || blank(command.appCode())
                || blank(command.totpSecret()) || blank(command.idempotencyKey())
                || command.idempotencyKey().length() > 128) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "MFA挑战请求不合法");
        }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    private static MfaChallengeAggregate restore(MfaMapper.ChallengeRow row) {
        return MfaChallengeAggregate.restore(row.id(), row.challengeNo(), row.userId(), row.appCode(),
                row.secretCiphertext(), row.idempotencyKey(), row.failedAttempts(), row.maxAttempts(),
                row.expiresAt(), MfaChallengeAggregate.Status.valueOf(row.challengeStatus()),
                row.verifiedAt(), row.version());
    }

    private static MfaMapper.ChallengeRow toRow(MfaChallengeAggregate value) {
        return new MfaMapper.ChallengeRow(value.id(), value.challengeNo(), value.userId(), value.appCode(),
                "TOTP", value.secretCiphertext(), value.status().name(), value.failedAttempts(),
                value.maxAttempts(), value.expiresAt(), value.verifiedAt(), value.idempotencyKey(), value.version());
    }

    private static ChallengeView view(MfaMapper.ChallengeRow row, boolean idempotentHit) {
        return new ChallengeView(row.challengeNo(), row.userId(), row.appCode(), row.factorType(),
                row.challengeStatus(), row.failedAttempts(), row.maxAttempts(), row.expiresAt(),
                row.verifiedAt(), row.version(), idempotentHit);
    }

    public record CreateCommand(long userId, String appCode, String totpSecret, String idempotencyKey) { }
    public record VerifyCommand(VerificationMethod method, String code) { }
    public enum VerificationMethod { TOTP, RECOVERY_CODE }
    public record ChallengeView(String challengeNo, long userId, String appCode, String factorType,
                                String status, int failedAttempts, int maxAttempts, Instant expiresAt,
                                Instant verifiedAt, int version, boolean idempotentHit) { }
    public record VerificationResult(String challengeNo, boolean verified, String status, Instant verifiedAt) { }
}
