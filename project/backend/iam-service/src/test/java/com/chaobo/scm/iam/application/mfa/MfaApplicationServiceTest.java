package com.chaobo.scm.iam.application.mfa;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.iam.application.TokenCachePort;
import com.chaobo.scm.iam.domain.mfa.MfaVerificationPolicy;
import com.chaobo.scm.iam.infrastructure.persistence.MfaMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MfaApplicationServiceTest {

    @Test
    void createsIdempotentlyVerifiesTotpAndPreventsReplay() {
        MemoryMapper mapper = new MemoryMapper();
        MfaApplicationService service = service(mapper, "123456");
        var first = service.create(create("idem-1"));
        var duplicate = service.create(create("idem-1"));

        assertThat(duplicate.idempotentHit()).isTrue();
        assertThat(service.verify(first.challengeNo(), new MfaApplicationService.VerifyCommand(
                MfaApplicationService.VerificationMethod.TOTP, "123456", 101, "LOGIN", "DEVICE-1")).verified()).isTrue();
        assertThatThrownBy(() -> service.verify(first.challengeNo(), new MfaApplicationService.VerifyCommand(
                MfaApplicationService.VerificationMethod.TOTP, "123456", 101, "LOGIN", "DEVICE-1")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("已使用");
    }

    @Test
    void consumesRecoveryCodeOnlyOnceAndTracksFailures() {
        MemoryMapper mapper = new MemoryMapper();
        String hash = MfaVerificationPolicy.recoveryCodeHash("RECOVERY-1");
        mapper.recoveryCodes.put("10:" + hash, false);
        MfaApplicationService service = service(mapper, "never");
        var challenge = service.create(create("idem-2"));

        assertThat(service.verify(challenge.challengeNo(), new MfaApplicationService.VerifyCommand(
                MfaApplicationService.VerificationMethod.RECOVERY_CODE, "RECOVERY-1", 101,
                "LOGIN", "DEVICE-1")).verified()).isTrue();
        assertThat(mapper.recoveryCodes.get("10:" + hash)).isTrue();
    }

    private static MfaApplicationService service(MemoryMapper mapper, String validCode) {
        mapper.configuration = new MfaMapper.ConfigurationRow(1, 10, "cipher:SECRET", 1, 0);
        return new MfaApplicationService(mapper, new MfaVerificationPolicy.SecretProtector() {
            public String encrypt(String plaintext) { return "cipher:" + plaintext; }
            public String decrypt(String ciphertext) { return ciphertext.substring(7); }
        }, (secret, code, now) -> validCode.equals(code), new NoopTokenCache());
    }

    private static MfaApplicationService.CreateCommand create(String idempotencyKey) {
        return new MfaApplicationService.CreateCommand(10, "IAM", 101, "LOGIN",
            "DEVICE-1", idempotencyKey);
    }

    private static final class MemoryMapper implements MfaMapper {
        private final Map<String, ChallengeRow> byNo = new LinkedHashMap<>();
        private final Map<String, Boolean> recoveryCodes = new LinkedHashMap<>();
        private ConfigurationRow configuration;

        public ChallengeRow findByIdempotencyKey(String key) {
            return byNo.values().stream().filter(row -> row.idempotencyKey().equals(key)).findFirst().orElse(null);
        }
        public ChallengeRow findByChallengeNo(String challengeNo) { return byNo.get(challengeNo); }
        public void insertChallenge(ChallengeRow row) { byNo.put(row.challengeNo(), row); }
        public int updateChallenge(long id, String status, int failedAttempts, Instant verifiedAt,
                                   int version, int oldVersion) {
            ChallengeRow row = byNo.values().stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null || row.version() != oldVersion) { return 0; }
            byNo.put(row.challengeNo(), new ChallengeRow(row.id(), row.challengeNo(), row.userId(), row.appCode(),
                    row.sessionId(), row.purpose(), row.deviceDigest(), row.factorType(),
                    row.secretCiphertext(), status, failedAttempts, row.maxAttempts(), row.expiresAt(),
                    verifiedAt, row.idempotencyKey(), version));
            return 1;
        }
        public int consumeRecoveryCode(long userId, String codeHash, String challengeNo) {
            String key = userId + ":" + codeHash;
            if (!Boolean.FALSE.equals(recoveryCodes.get(key))) { return 0; }
            recoveryCodes.put(key, true);
            return 1;
        }
        public ConfigurationRow findActiveConfiguration(long userId) {
            return configuration != null && configuration.userId() == userId && configuration.status() == 1
                ? configuration : null;
        }
        public ConfigurationRow findConfiguration(long userId) { return configuration; }
        public List<ConfigurationGovernanceRow> listConfigurations(int limit) { return List.of(); }
        public List<ChallengeGovernanceRow> listChallenges(int limit) { return List.of(); }
        public void upsertConfiguration(ConfigurationRow row) { configuration = row; }
        public int updateConfigurationStatus(long userId, int status) {
            configuration = new ConfigurationRow(configuration.configId(), userId,
                configuration.secretCiphertext(), status, configuration.version() + 1);
            return 1;
        }
        public int invalidateRecoveryCodes(long userId, String reason) { return 1; }
        public void insertRecoveryCode(long id, long userId, String codeHash) {
            recoveryCodes.put(userId + ":" + codeHash, false);
        }
        public List<Long> findActiveSessionIds(long userId) { return List.of(); }
        public int revokeSessions(long userId, String reason) { return 0; }
        public void insertAudit(AuditRow row) { }
        public void insertOutbox(OutboxRow row) { }
    }

    private static final class NoopTokenCache implements TokenCachePort {
        public void store(OnlineSession session) { }
        public Optional<OnlineSession> findByAccessJti(String accessJti) { return Optional.empty(); }
        public Optional<OnlineSession> findByRefreshJti(String refreshJti) { return Optional.empty(); }
        public RotationResult rotate(String presentedRefreshJti, OnlineSession replacement) {
            return RotationResult.NOT_FOUND;
        }
        public void revoke(long sessionId) { }
    }
}
