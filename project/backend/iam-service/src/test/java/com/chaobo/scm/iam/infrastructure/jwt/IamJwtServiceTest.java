package com.chaobo.scm.iam.infrastructure.jwt;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IamJwtServiceTest {
    @Test
    void signedJwtCanBeVerifiedAndTamperingIsRejected() {
        IamJwtService service = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();

        String token = service.issue(new IamJwtService.TokenClaims(
                "1001", "admin", "IAM", "AT-1", "ACCESS", now, now + 60));
        IamJwtService.TokenClaims claims = service.verify(token);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(claims.username()).isEqualTo("admin");
        assertThatThrownBy(() -> service.verify(tampered)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void signedJwtCarriesPermissionAndDataScopeClaims() {
        IamJwtService service = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();

        String token = service.issue(new IamJwtService.TokenClaims(
                "1001", "buyer", "PURCHASE", "AT-2", "ACCESS", now, now + 60,
                Set.of("purchase:po:read", "purchase:po:create"),
                Map.of("PURCHASE_ORG", Set.of("ORG-1", "ORG-2"))));

        IamJwtService.TokenClaims claims = service.verify(token);

        assertThat(claims.permissions()).containsExactlyInAnyOrder("purchase:po:read", "purchase:po:create");
        assertThat(claims.dataScopes().get("PURCHASE_ORG")).containsExactlyInAnyOrder("ORG-1", "ORG-2");
    }

    @Test
    void weakSigningSecretIsRejected() {
        assertThatThrownBy(() -> new IamJwtService("short-secret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32");
    }
}
