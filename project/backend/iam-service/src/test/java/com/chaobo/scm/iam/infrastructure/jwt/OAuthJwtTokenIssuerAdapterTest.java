package com.chaobo.scm.iam.infrastructure.jwt;

import com.chaobo.scm.iam.application.IamTokenClaimsProvider;
import com.chaobo.scm.iam.application.OAuthTokenIssuerPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthJwtTokenIssuerAdapterTest {

    @Test
    void authorizationCodeTokensCarryUserClaimsWhileClientCredentialsStayMachineOnly() {
        long now = Instant.parse("2026-07-30T01:00:00Z").getEpochSecond();
        IamJwtService jwt = new IamJwtService("oauth-kid",
                "01234567890123456789012345678901", Map.of(), () -> now);
        IamTokenClaimsProvider claims = ignored -> new IamTokenClaimsProvider.PermissionClaims(
                Set.of("order.read"), Map.of("warehouse", Set.of("WH-01")));
        OAuthJwtTokenIssuerAdapter adapter = new OAuthJwtTokenIssuerAdapter(jwt, claims, id -> "user-" + id,
                () -> now);

        OAuthTokenIssuerPort.IssuedTokens userTokens = adapter.issueAuthorizationCodeTokens(
                new OAuthTokenIssuerPort.AuthorizationCodeTokenCommand(1001L, "OMS-WEB", "OMS",
                        Set.of("openid", "profile"), "nonce-1", 900, 300));
        OAuthTokenIssuerPort.IssuedTokens machineTokens = adapter.issueClientCredentialsToken(
                new OAuthTokenIssuerPort.ClientCredentialsTokenCommand("OMS-WEB", "OMS",
                        Set.of("inventory.read"), 900));

        IamJwtService.TokenClaims userAccess = jwt.verify(userTokens.accessToken());
        IamJwtService.TokenClaims machineAccess = jwt.verify(machineTokens.accessToken());
        assertThat(userAccess.subject()).isEqualTo("1001");
        assertThat(userAccess.permissions()).contains("order.read", "openid", "profile");
        assertThat(userAccess.dataScopes()).containsKey("warehouse");
        assertThat(jwt.verify(userTokens.idToken()).tokenType()).isEqualTo("OIDC_ID");
        assertThat(userTokens.refreshToken()).isNotBlank().doesNotContain("1001");
        assertThat(machineAccess.subject()).isEqualTo("client:OMS-WEB");
        assertThat(machineAccess.permissions()).containsExactly("inventory.read");
        assertThat(machineAccess.dataScopes()).isEmpty();
        assertThat(machineTokens.refreshToken()).isNull();
        assertThat(machineTokens.idToken()).isNull();
        assertThat(adapter.userInfo(userTokens.accessToken()).username()).isEqualTo("user-1001");
    }
}
