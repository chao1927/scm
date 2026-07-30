package com.chaobo.scm.iam.domain.mfa;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MfaVerificationPolicyTest {

    @Test
    void verifiesStandardTotpAndEncryptsSecretWithoutPlaintextLeakage() {
        var verifier = new MfaVerificationPolicy.StandardTotpVerifier();
        assertThat(verifier.verify("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "287082",
                Instant.ofEpochSecond(59))).isTrue();

        var protector = new MfaVerificationPolicy.AesGcmSecretProtector(
                "01234567890123456789012345678901");
        String encrypted = protector.encrypt("TOP-SECRET");
        assertThat(encrypted).doesNotContain("TOP-SECRET");
        assertThat(protector.decrypt(encrypted)).isEqualTo("TOP-SECRET");
    }
}
