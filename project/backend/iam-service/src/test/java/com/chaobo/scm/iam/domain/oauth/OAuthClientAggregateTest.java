package com.chaobo.scm.iam.domain.oauth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthClientAggregateTest {

    @Test
    void acceptsExactRedirectScopeAndPkceS256() {
        OAuthClientAggregate client = client();

        client.validateAuthorizationRequest(
                "https://oms.example/callback",
                Set.of("openid", "profile"),
                "S256"
        );

        assertThat(client.matchesSecret("high-entropy-secret")).isTrue();
    }

    @Test
    void rejectsRedirectPrefixPlainPkceAndExcessScope() {
        OAuthClientAggregate client = client();

        assertThatThrownBy(() -> client.validateAuthorizationRequest(
                "https://oms.example/callback/attacker",
                Set.of("openid"),
                "S256"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("redirect_uri");
        assertThatThrownBy(() -> client.validateAuthorizationRequest(
                "https://oms.example/callback",
                Set.of("openid"),
                "plain"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PKCE");
        assertThatThrownBy(() -> client.validateAuthorizationRequest(
                "https://oms.example/callback",
                Set.of("openid", "admin"),
                "S256"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scope");
    }

    @Test
    void clientCredentialsRequiresConfidentialClientAndValidSecret() {
        OAuthClientAggregate client = client();

        client.validateClientCredentials("high-entropy-secret", Set.of("inventory.read"));

        assertThatThrownBy(() -> client.validateClientCredentials("wrong", Set.of("inventory.read")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("secret");
    }

    private OAuthClientAggregate client() {
        return OAuthClientAggregate.restore(
                "OMS-WEB",
                "OMS",
                OAuthClientAggregate.ClientType.CONFIDENTIAL,
                OAuthClientAggregate.hashSecret("high-entropy-secret"),
                Set.of("https://oms.example/callback"),
                Set.of("authorization_code", "client_credentials"),
                Set.of("openid", "profile", "inventory.read"),
                900,
                300,
                OAuthClientAggregate.ENABLED
        );
    }
}
