package com.chaobo.scm.iam.application.mfa;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.iam.domain.mfa.MfaVerificationPolicy;
import com.chaobo.scm.iam.infrastructure.persistence.MfaMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MfaApplicationServiceTest {

    @Test
    void createsIdempotentlyVerifiesTotpAndPreventsReplay() {
        MemoryMapper mapper = new MemoryMapper();
        MfaApplicationService service = service(mapper, "123456");
        var first = service.create(new MfaApplicationService.CreateCommand(10, "IAM", "SECRET", "idem-1"));
        var duplicate = service.create(new MfaApplicationService.CreateCommand(10, "IAM", "SECRET", "idem-1"));

        assertThat(duplicate.idempotentHit()).isTrue();
        assertThat(service.verify(first.challengeNo(), new MfaApplicationService.VerifyCommand(
                MfaApplicationService.VerificationMethod.TOTP, "123456")).verified()).isTrue();
        assertThatThrownBy(() -> service.verify(first.challengeNo(), new MfaApplicationService.VerifyCommand(
                MfaApplicationService.VerificationMethod.TOTP, "123456")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("已使用");
    }

    @Test
    void consumesRecoveryCodeOnlyOnceAndTracksFailures() {
        MemoryMapper mapper = new MemoryMapper();
        String hash = MfaVerificationPolicy.recoveryCodeHash("RECOVERY-1");
        mapper.recoveryCodes.put("10:" + hash, false);
        MfaApplicationService service = service(mapper, "never");
        var challenge = service.create(new MfaApplicationService.CreateCommand(10, "IAM", "SECRET", "idem-2"));

        assertThat(service.verify(challenge.challengeNo(), new MfaApplicationService.VerifyCommand(
                MfaApplicationService.VerificationMethod.RECOVERY_CODE, "RECOVERY-1")).verified()).isTrue();
        assertThat(mapper.recoveryCodes.get("10:" + hash)).isTrue();
    }

    private static MfaApplicationService service(MemoryMapper mapper, String validCode) {
        return new MfaApplicationService(mapper, new MfaVerificationPolicy.SecretProtector() {
            public String encrypt(String plaintext) { return "cipher:" + plaintext; }
            public String decrypt(String ciphertext) { return ciphertext.substring(7); }
        }, (secret, code, now) -> validCode.equals(code));
    }

    private static final class MemoryMapper implements MfaMapper {
        private final Map<String, ChallengeRow> byNo = new LinkedHashMap<>();
        private final Map<String, Boolean> recoveryCodes = new LinkedHashMap<>();

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
                    row.factorType(), row.secretCiphertext(), status, failedAttempts, row.maxAttempts(),
                    row.expiresAt(), verifiedAt, row.idempotencyKey(), version));
            return 1;
        }
        public int consumeRecoveryCode(long userId, String codeHash, String challengeNo) {
            String key = userId + ":" + codeHash;
            if (!Boolean.FALSE.equals(recoveryCodes.get(key))) { return 0; }
            recoveryCodes.put(key, true);
            return 1;
        }
    }
}
