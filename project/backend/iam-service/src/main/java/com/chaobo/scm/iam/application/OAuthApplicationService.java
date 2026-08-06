package com.chaobo.scm.iam.application;

import com.chaobo.scm.iam.domain.oauth.AuthorizationCodeAggregate;
import com.chaobo.scm.iam.domain.oauth.OAuthClientAggregate;
import com.chaobo.scm.iam.infrastructure.persistence.OAuthMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OAuth 2.0 authorization-code/PKCE and client-credentials application use cases.
 *
 * @author chaobo
 */
@SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
@Service
public class OAuthApplicationService {

    private static final int AUTHORIZATION_CODE_TTL_SECONDS = 120;
    private static final int REFRESH_TOKEN_TTL_SECONDS = 30 * 24 * 60 * 60;
    private static final int RANDOM_TOKEN_BYTES = 32;
    private static final String OPENID_SCOPE = "openid";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OAuthMapper mapper;
    private final OAuthTokenIssuerPort tokenIssuer;
    private final Clock clock;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    @Autowired
    public OAuthApplicationService(OAuthMapper mapper, OAuthTokenIssuerPort tokenIssuer) {
        this(mapper, tokenIssuer, Clock.systemUTC());
    }

    OAuthApplicationService(OAuthMapper mapper, OAuthTokenIssuerPort tokenIssuer, Clock clock) {
        this.mapper = mapper;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        requireAuthorizationRequest(request);
        OAuthClientAggregate client = requiredClient(request.clientId());
        client.validateAuthorizationRequest(request.redirectUri(), request.scopes(), request.codeChallengeMethod());
        if (mapper.findEnabledUser(request.userId()) == null) {
            throw new IllegalArgumentException("user is not enabled");
        }
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusSeconds(AUTHORIZATION_CODE_TTL_SECONDS);
        String rawCode = randomToken();
        AuthorizationCodeAggregate code = AuthorizationCodeAggregate.issue(
                hash(rawCode), request.clientId(), request.userId(), request.redirectUri(), request.scopes(),
                request.codeChallenge(), request.nonce(), request.requestId(), issuedAt, expiresAt);
        mapper.insertAuthorizationCode(toRow(code));
        audit("AUTHORIZATION_CODE_ISSUED", client.clientId(), request.userId(), request.requestId(),
                "scopes=" + scopesText(request.scopes()), issuedAt);
        outbox("OAuthAuthorizationCodeIssued", request.requestId(), client.clientId(), issuedAt);
        return new AuthorizationResponse(rawCode, request.state(), request.redirectUri(), AUTHORIZATION_CODE_TTL_SECONDS);
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenResponse exchangeAuthorizationCode(AuthorizationCodeTokenRequest request) {
        requireAuthorizationCodeRequest(request);
        Instant now = clock.instant();
        String codeHash = hash(request.code());
        OAuthMapper.AuthorizationCodeRow row = mapper.findAuthorizationCode(codeHash);
        if (row == null) {
            throw new IllegalArgumentException("authorization code is invalid");
        }
        AuthorizationCodeAggregate code = restore(row);
        code.validateForConsumption(request.clientId(), request.redirectUri(), request.codeVerifier(), now);
        OAuthClientAggregate client = requiredClient(request.clientId());
        client.validateAuthorizationCodeClient(request.clientSecret(), code.scopes());
        if (mapper.consumeAuthorizationCode(codeHash, request.clientId(), request.redirectUri(), now) != 1) {
            throw new IllegalStateException("authorization code was already consumed");
        }
        OAuthTokenIssuerPort.IssuedTokens tokens = tokenIssuer.issueAuthorizationCodeTokens(
                new OAuthTokenIssuerPort.AuthorizationCodeTokenCommand(code.userId(), client.clientId(),
                        client.appCode(), code.scopes(), code.nonce(), client.accessTtlSeconds(),
                        client.idTokenTtlSeconds()));
        if (tokens.refreshToken() != null) {
            String grantId = randomToken();
            mapper.insertGrant(new OAuthMapper.GrantRow(grantId, client.clientId(), code.userId(),
                    scopesText(code.scopes()), now));
            mapper.insertRefreshToken(new OAuthMapper.RefreshTokenRow(hash(tokens.refreshToken()), grantId, 1,
                    now.plusSeconds(REFRESH_TOKEN_TTL_SECONDS), null, now));
        }
        audit("AUTHORIZATION_CODE_EXCHANGED", client.clientId(), code.userId(), code.requestId(),
                "scopes=" + scopesText(code.scopes()), now);
        outbox("OAuthAuthorizationCodeExchanged", code.requestId(), client.clientId(), now);
        return response(tokens);
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = RefreshTokenReuseException.class)
    public TokenResponse refresh(RefreshTokenRequest request) {
        if (request == null || blank(request.clientId()) || blank(request.clientSecret())
                || blank(request.refreshToken())) {
            throw new IllegalArgumentException("refresh token request is invalid");
        }
        Instant now = clock.instant();
        OAuthMapper.RefreshGrantRow row = mapper.findRefreshGrant(hash(request.refreshToken()));
        if (row == null || row.grantStatus() != 1 || row.revokedAt() != null) {
            throw new IllegalArgumentException("refresh token is invalid or revoked");
        }
        OAuthClientAggregate client = requiredClient(request.clientId());
        Set<String> scopes = words(row.scopes());
        client.validateAuthorizationCodeClient(request.clientSecret(), scopes);
        if (!row.clientId().equals(request.clientId())) {
            throw new IllegalArgumentException("refresh token client mismatch");
        }
        if (row.consumedAt() != null || !row.expiresAt().isAfter(now)
                || mapper.consumeRefreshToken(row.tokenHash(), now) != 1) {
            mapper.revokeGrant(row.grantId(), now);
            audit("REFRESH_TOKEN_REUSE_DETECTED", row.clientId(), row.userId(), row.grantId(),
                    "generation=" + row.generation(), now);
            outbox("OAuthGrantRevoked", row.grantId(), row.clientId(), now);
            throw new RefreshTokenReuseException("refresh token reuse revoked the authorization grant");
        }
        OAuthTokenIssuerPort.IssuedTokens tokens = tokenIssuer.issueRefreshTokenTokens(
                new OAuthTokenIssuerPort.AuthorizationCodeTokenCommand(row.userId(), client.clientId(),
                        client.appCode(), scopes, null, client.accessTtlSeconds(), client.idTokenTtlSeconds()));
        if (tokens.refreshToken() == null) {
            throw new IllegalStateException("refresh rotation did not issue a replacement token");
        }
        mapper.insertRefreshToken(new OAuthMapper.RefreshTokenRow(hash(tokens.refreshToken()), row.grantId(),
                row.generation() + 1, now.plusSeconds(REFRESH_TOKEN_TTL_SECONDS), null, now));
        audit("REFRESH_TOKEN_ROTATED", row.clientId(), row.userId(), row.grantId(),
                "generation=" + (row.generation() + 1), now);
        return response(tokens);
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(RevokeTokenRequest request) {
        if (request == null || blank(request.clientId()) || blank(request.clientSecret()) || blank(request.token())) {
            throw new IllegalArgumentException("revoke request is invalid");
        }
        OAuthMapper.RefreshGrantRow row = mapper.findRefreshGrant(hash(request.token()));
        if (row == null) {
            return;
        }
        OAuthClientAggregate client = requiredClient(request.clientId());
        client.validateAuthorizationCodeClient(request.clientSecret(), words(row.scopes()));
        if (!row.clientId().equals(request.clientId())) {
            throw new IllegalArgumentException("token client mismatch");
        }
        Instant now = clock.instant();
        mapper.revokeGrant(row.grantId(), now);
        audit("OAUTH_GRANT_REVOKED", row.clientId(), row.userId(), row.grantId(), "client request", now);
        outbox("OAuthGrantRevoked", row.grantId(), row.clientId(), now);
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenResponse issueClientCredentials(ClientCredentialsTokenRequest request) {
        if (request == null || blank(request.clientId()) || blank(request.clientSecret())
                || request.scopes() == null || request.scopes().isEmpty()) {
            throw new IllegalArgumentException("client credentials request is invalid");
        }
        OAuthClientAggregate client = requiredClient(request.clientId());
        client.validateClientCredentials(request.clientSecret(), request.scopes());
        Instant now = clock.instant();
        OAuthTokenIssuerPort.IssuedTokens tokens = tokenIssuer.issueClientCredentialsToken(
                new OAuthTokenIssuerPort.ClientCredentialsTokenCommand(client.clientId(), client.appCode(),
                        request.scopes(), client.accessTtlSeconds()));
        audit("CLIENT_CREDENTIALS_TOKEN_ISSUED", client.clientId(), null, client.clientId(),
                "scopes=" + scopesText(request.scopes()), now);
        outbox("OAuthClientCredentialsTokenIssued", client.clientId(), client.clientId(), now);
        return response(tokens);
    }

    public OAuthTokenIssuerPort.UserInfo userInfo(String bearerToken) {
        if (blank(bearerToken)) {
            throw new IllegalArgumentException("bearer token is required");
        }
        return tokenIssuer.userInfo(bearerToken);
    }

    public static String pkceChallenge(String verifier) {
        if (blank(verifier)) {
            throw new IllegalArgumentException("PKCE verifier is required");
        }
        return AuthorizationCodeAggregate.pkceChallenge(verifier);
    }

    private OAuthClientAggregate requiredClient(String clientId) {
        OAuthMapper.OAuthClientRow row = mapper.findEnabledClient(clientId);
        if (row == null) {
            throw new IllegalArgumentException("OAuth client is invalid or disabled");
        }
        return OAuthClientAggregate.restore(row.clientId(), row.appCode(),
                OAuthClientAggregate.ClientType.valueOf(row.clientType()), row.secretHash(),
                words(row.redirectUris()), words(row.grantTypes()), words(row.allowedScopes()),
                row.accessTtlSeconds(), row.idTokenTtlSeconds(), row.status());
    }

    private void audit(String action, String clientId, Long userId, String requestId, String detail, Instant at) {
        mapper.insertAudit(new OAuthMapper.AuditRow(ids.incrementAndGet(), action, clientId, userId,
                requestId, detail, at));
    }

    private void outbox(String eventType, String businessNo, String clientId, Instant at) {
        mapper.insertOutbox(new OAuthMapper.OutboxRow(ids.incrementAndGet(), eventType, businessNo,
                "clientId=" + clientId, at));
    }

    private static OAuthMapper.AuthorizationCodeRow toRow(AuthorizationCodeAggregate code) {
        return new OAuthMapper.AuthorizationCodeRow(code.codeHash(), code.clientId(), code.userId(),
                code.redirectUri(), scopesText(code.scopes()), code.codeChallenge(), code.nonce(),
                code.requestId(), code.issuedAt(), code.expiresAt(), code.consumedAt());
    }

    private static AuthorizationCodeAggregate restore(OAuthMapper.AuthorizationCodeRow row) {
        return AuthorizationCodeAggregate.restore(row.codeHash(), row.clientId(), row.userId(), row.redirectUri(),
                words(row.scopes()), row.codeChallenge(), row.nonce(), row.requestId(), row.issuedAt(),
                row.expiresAt(), row.consumedAt());
    }

    private static TokenResponse response(OAuthTokenIssuerPort.IssuedTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.idToken(),
                tokens.tokenType(), tokens.expiresIn(), tokens.scopes());
    }

    private static Set<String> words(String value) {
        if (blank(value)) {
            return Set.of();
        }
        return Set.copyOf(new LinkedHashSet<>(Arrays.asList(value.trim().split("\\s+"))));
    }

    private static String scopesText(Set<String> scopes) {
        return String.join(" ", new java.util.TreeSet<>(scopes));
    }

    private static String randomToken() {
        byte[] bytes = new byte[RANDOM_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String value) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void requireAuthorizationRequest(AuthorizationRequest request) {
        if (request == null || blank(request.clientId()) || request.userId() <= 0 || blank(request.redirectUri())
                || request.scopes() == null || request.scopes().isEmpty() || blank(request.codeChallenge())
                || blank(request.codeChallengeMethod()) || blank(request.state()) || blank(request.requestId())) {
            throw new IllegalArgumentException("authorization request is invalid");
        }
        if (request.scopes().contains(OPENID_SCOPE) && blank(request.nonce())) {
            throw new IllegalArgumentException("OIDC nonce is required for openid scope");
        }
    }

    private static void requireAuthorizationCodeRequest(AuthorizationCodeTokenRequest request) {
        if (request == null || blank(request.clientId()) || blank(request.code()) || blank(request.redirectUri())
                || blank(request.codeVerifier())) {
            throw new IllegalArgumentException("authorization code token request is invalid");
        }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    public record AuthorizationRequest(String clientId, long userId, String redirectUri, Set<String> scopes,
                                       String codeChallenge, String codeChallengeMethod, String state,
                                       String nonce, String requestId) { }
    public record AuthorizationResponse(String code, String state, String redirectUri, int expiresIn) { }
    public record AuthorizationCodeTokenRequest(String clientId, String clientSecret, String code,
                                                String redirectUri, String codeVerifier) { }
    public record ClientCredentialsTokenRequest(String clientId, String clientSecret, Set<String> scopes) { }
    public record RefreshTokenRequest(String clientId, String clientSecret, String refreshToken) { }
    public record RevokeTokenRequest(String clientId, String clientSecret, String token) { }
    public record TokenResponse(String accessToken, String refreshToken, String idToken, String tokenType,
                                int expiresIn, Set<String> scopes) { }

    public static class RefreshTokenReuseException extends IllegalStateException {
        public RefreshTokenReuseException(String message) { super(message); }
    }
}
