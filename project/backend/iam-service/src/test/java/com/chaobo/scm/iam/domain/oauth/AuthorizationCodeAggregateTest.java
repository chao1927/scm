package com.chaobo.scm.iam.domain.oauth;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationCodeAggregateTest {

    @Test
    void consumesOnceWithMatchingClientRedirectAndVerifier() {
        Instant issuedAt = Instant.parse("2026-07-30T01:00:00Z");
        String verifier = "0123456789012345678901234567890123456789012";
        AuthorizationCodeAggregate code = AuthorizationCodeAggregate.issue(
                "code-hash",
                "OMS-WEB",
                1001L,
                "https://oms.example/callback",
                Set.of("openid", "profile"),
                challenge(verifier),
                "nonce-1",
                "request-1",
                issuedAt,
                issuedAt.plusSeconds(120)
        );

        code.validateForConsumption(
                "OMS-WEB",
                "https://oms.example/callback",
                verifier,
                issuedAt.plusSeconds(30)
        );
    }

    @Test
    void rejectsExpiredWrongRedirectWrongVerifierAndReplay() {
        Instant issuedAt = Instant.parse("2026-07-30T01:00:00Z");
        String verifier = "0123456789012345678901234567890123456789012";
        AuthorizationCodeAggregate code = AuthorizationCodeAggregate.issue(
                "code-hash",
                "OMS-WEB",
                1001L,
                "https://oms.example/callback",
                Set.of("openid"),
                challenge(verifier),
                "nonce-1",
                "request-1",
                issuedAt,
                issuedAt.plusSeconds(120)
        );

        assertThatThrownBy(() -> code.validateForConsumption(
                "OMS-WEB",
                "https://evil.example/callback",
                verifier,
                issuedAt.plusSeconds(30)
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> code.validateForConsumption(
                "OMS-WEB",
                "https://oms.example/callback",
                verifier + "x",
                issuedAt.plusSeconds(30)
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> code.validateForConsumption(
                "OMS-WEB",
                "https://oms.example/callback",
                verifier,
                issuedAt.plusSeconds(121)
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");

    }

    private String challenge(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
