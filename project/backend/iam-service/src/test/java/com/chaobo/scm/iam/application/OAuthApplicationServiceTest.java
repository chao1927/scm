package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.domain.oauth.OAuthClientAggregate;
import com.chaobo.scm.iam.infrastructure.persistence.OAuthMapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-30T01:00:00Z");

    @Test
    void authorizationCodeFlowPersistsHashConsumesAtomicallyAndReturnsOidcTokens() {
        MemoryOAuthMapper mapper = new MemoryOAuthMapper();
        FakeTokenIssuer issuer = new FakeTokenIssuer();
        OAuthApplicationService service = service(mapper, issuer);
        String verifier = "0123456789012345678901234567890123456789012";

        OAuthApplicationService.AuthorizationResponse authorization = service.authorize(
                new OAuthApplicationService.AuthorizationRequest(
                        "OMS-WEB",
                        1001L,
                        "https://oms.example/callback",
                        Set.of("openid", "profile"),
                        OAuthApplicationService.pkceChallenge(verifier),
                        "S256",
                        "state-1",
                        "nonce-1",
                        "request-1"
                )
        );
        OAuthApplicationService.TokenResponse token = service.exchangeAuthorizationCode(
                new OAuthApplicationService.AuthorizationCodeTokenRequest(
                        "OMS-WEB",
                        "high-entropy-secret",
                        authorization.code(),
                        "https://oms.example/callback",
                        verifier
                )
        );

        assertThat(mapper.authorizationCodes).hasSize(1);
        assertThat(mapper.authorizationCodes.values().iterator().next().codeHash())
                .doesNotContain(authorization.code());
        assertThat(mapper.consumeCount).isEqualTo(1);
        assertThat(token.accessToken()).isEqualTo("access-user");
        assertThat(token.idToken()).isEqualTo("id-user");
        assertThat(token.refreshToken()).isEqualTo("refresh-user");
        assertThat(issuer.lastAuthorizationCommand.nonce()).isEqualTo("nonce-1");
        assertThat(mapper.audits).containsKeys("AUTHORIZATION_CODE_ISSUED", "AUTHORIZATION_CODE_EXCHANGED");
    }

    @Test
    void replayedAuthorizationCodeFailsAfterAtomicConsumption() {
        MemoryOAuthMapper mapper = new MemoryOAuthMapper();
        OAuthApplicationService service = service(mapper, new FakeTokenIssuer());
        String verifier = "0123456789012345678901234567890123456789012";
        OAuthApplicationService.AuthorizationResponse authorization = service.authorize(
                new OAuthApplicationService.AuthorizationRequest(
                        "OMS-WEB",
                        1001L,
                        "https://oms.example/callback",
                        Set.of("openid"),
                        OAuthApplicationService.pkceChallenge(verifier),
                        "S256",
                        "state-1",
                        "nonce-1",
                        "request-1"
                )
        );
        OAuthApplicationService.AuthorizationCodeTokenRequest request =
                new OAuthApplicationService.AuthorizationCodeTokenRequest(
                        "OMS-WEB",
                        "high-entropy-secret",
                        authorization.code(),
                        "https://oms.example/callback",
                        verifier
                );

        service.exchangeAuthorizationCode(request);

        assertThatThrownBy(() -> service.exchangeAuthorizationCode(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("consumed");
    }

    @Test
    void clientCredentialsIssuesAccessTokenWithoutRefreshOrIdToken() {
        MemoryOAuthMapper mapper = new MemoryOAuthMapper();
        OAuthApplicationService service = service(mapper, new FakeTokenIssuer());

        OAuthApplicationService.TokenResponse token = service.issueClientCredentials(
                new OAuthApplicationService.ClientCredentialsTokenRequest(
                        "OMS-WEB",
                        "high-entropy-secret",
                        Set.of("inventory.read")
                )
        );

        assertThat(token.accessToken()).isEqualTo("access-client");
        assertThat(token.refreshToken()).isNull();
        assertThat(token.idToken()).isNull();
        assertThat(mapper.audits).containsKey("CLIENT_CREDENTIALS_TOKEN_ISSUED");
    }

    private OAuthApplicationService service(MemoryOAuthMapper mapper, OAuthTokenIssuerPort issuer) {
        return new OAuthApplicationService(
                mapper,
                issuer,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    static class FakeTokenIssuer implements OAuthTokenIssuerPort {

        AuthorizationCodeTokenCommand lastAuthorizationCommand;

        @Override
        public IssuedTokens issueAuthorizationCodeTokens(AuthorizationCodeTokenCommand command) {
            lastAuthorizationCommand = command;
            return new IssuedTokens("access-user", "refresh-user", "id-user", "Bearer", 900, Set.of("openid", "profile"));
        }

        @Override
        public IssuedTokens issueClientCredentialsToken(ClientCredentialsTokenCommand command) {
            return new IssuedTokens("access-client", null, null, "Bearer", 900, command.scopes());
        }

        @Override
        public UserInfo userInfo(String bearerToken) {
            return new UserInfo("1001", "user-1001", "测试用户", Set.of("profile"));
        }
    }

    static class MemoryOAuthMapper implements OAuthMapper {

        final Map<String, AuthorizationCodeRow> authorizationCodes = new LinkedHashMap<>();
        final Map<String, AuditRow> audits = new LinkedHashMap<>();
        final Map<String, GrantRow> grants = new LinkedHashMap<>();
        final Map<String, RefreshTokenRow> refreshTokens = new LinkedHashMap<>();
        int consumeCount;

        @Override
        public OAuthClientRow findEnabledClient(String clientId) {
            if (!"OMS-WEB".equals(clientId)) {
                return null;
            }
            return new OAuthClientRow(
                    "OMS-WEB",
                    "OMS",
                    "CONFIDENTIAL",
                    OAuthClientAggregate.hashSecret("high-entropy-secret"),
                    "https://oms.example/callback",
                    "authorization_code client_credentials",
                    "openid profile inventory.read",
                    900,
                    300,
                    1,
                    1
            );
        }

        @Override
        public Integer findEnabledUser(long userId) {
            return userId == 1001L ? 1 : null;
        }

        @Override
        public void insertAuthorizationCode(AuthorizationCodeRow row) {
            authorizationCodes.put(row.codeHash(), row);
        }

        @Override
        public AuthorizationCodeRow findAuthorizationCode(String codeHash) {
            return authorizationCodes.get(codeHash);
        }

        @Override
        public int consumeAuthorizationCode(String codeHash, String clientId, String redirectUri, Instant consumedAt) {
            AuthorizationCodeRow row = authorizationCodes.get(codeHash);
            if (row == null || row.consumedAt() != null || !row.clientId().equals(clientId)
                    || !row.redirectUri().equals(redirectUri) || !row.expiresAt().isAfter(consumedAt)) {
                return 0;
            }
            authorizationCodes.put(codeHash, row.consumed(consumedAt));
            consumeCount++;
            return 1;
        }

        @Override
        public void insertAudit(AuditRow row) {
            audits.put(row.action(), row);
        }

        @Override
        public void insertOutbox(OutboxRow row) {
        }

        @Override
        public void insertGrant(GrantRow row) {
            grants.put(row.grantId(), row);
        }

        @Override
        public void insertRefreshToken(RefreshTokenRow row) {
            refreshTokens.put(row.tokenHash(), row);
        }

        @Override
        public RefreshGrantRow findRefreshGrant(String tokenHash) {
            RefreshTokenRow token = refreshTokens.get(tokenHash);
            if (token == null) { return null; }
            GrantRow grant = grants.get(token.grantId());
            return new RefreshGrantRow(token.tokenHash(), token.grantId(), token.generation(), token.expiresAt(),
                    token.consumedAt(), grant.clientId(), grant.userId(), grant.scopes(), 1, null);
        }

        @Override
        public int consumeRefreshToken(String tokenHash, Instant consumedAt) {
            RefreshTokenRow token = refreshTokens.get(tokenHash);
            if (token == null || token.consumedAt() != null || !token.expiresAt().isAfter(consumedAt)) { return 0; }
            refreshTokens.put(tokenHash, new RefreshTokenRow(token.tokenHash(), token.grantId(), token.generation(),
                    token.expiresAt(), consumedAt, token.createdAt()));
            return 1;
        }

        @Override
        public int revokeGrant(String grantId, Instant revokedAt) {
            return grants.containsKey(grantId) ? 1 : 0;
        }
    }
}
