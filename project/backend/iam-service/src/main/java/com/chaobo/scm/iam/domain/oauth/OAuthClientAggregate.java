package com.chaobo.scm.iam.domain.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * OAuth 客户端聚合，保护授权类型、回调地址、Scope 与客户端密钥不变量。
 *
 * @author chaobo
 */
@SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
public final class OAuthClientAggregate {

    public static final int ENABLED = 1;
    private static final String PKCE_S256 = "S256";

    private final String clientId;
    private final String appCode;
    private final ClientType clientType;
    private final String secretHash;
    private final Set<String> redirectUris;
    private final Set<String> grantTypes;
    private final Set<String> allowedScopes;
    private final int accessTtlSeconds;
    private final int idTokenTtlSeconds;
    private final int status;

    private OAuthClientAggregate(
            String clientId,
            String appCode,
            ClientType clientType,
            String secretHash,
            Set<String> redirectUris,
            Set<String> grantTypes,
            Set<String> allowedScopes,
            int accessTtlSeconds,
            int idTokenTtlSeconds,
            int status
    ) {
        if (blank(clientId) || blank(appCode) || clientType == null) {
            throw new IllegalArgumentException("OAuth client identity is required");
        }
        if (clientType == ClientType.CONFIDENTIAL && blank(secretHash)) {
            throw new IllegalArgumentException("confidential client secret hash is required");
        }
        if (accessTtlSeconds <= 0 || idTokenTtlSeconds <= 0) {
            throw new IllegalArgumentException("token TTL must be positive");
        }
        this.clientId = clientId;
        this.appCode = appCode;
        this.clientType = clientType;
        this.secretHash = secretHash;
        this.redirectUris = Set.copyOf(redirectUris);
        this.grantTypes = Set.copyOf(grantTypes);
        this.allowedScopes = Set.copyOf(allowedScopes);
        this.accessTtlSeconds = accessTtlSeconds;
        this.idTokenTtlSeconds = idTokenTtlSeconds;
        this.status = status;
    }

    public static OAuthClientAggregate restore(
            String clientId,
            String appCode,
            ClientType clientType,
            String secretHash,
            Set<String> redirectUris,
            Set<String> grantTypes,
            Set<String> allowedScopes,
            int accessTtlSeconds,
            int idTokenTtlSeconds,
            int status
    ) {
        return new OAuthClientAggregate(
                clientId,
                appCode,
                clientType,
                secretHash,
                redirectUris,
                grantTypes,
                allowedScopes,
                accessTtlSeconds,
                idTokenTtlSeconds,
                status
        );
    }

    public void validateAuthorizationRequest(
            String redirectUri,
            Set<String> requestedScopes,
            String codeChallengeMethod
    ) {
        ensureEnabled();
        ensureGrantType("authorization_code");
        if (!redirectUris.contains(redirectUri)) {
            throw new IllegalArgumentException("redirect_uri must exactly match a registered URI");
        }
        if (!PKCE_S256.equals(codeChallengeMethod)) {
            throw new IllegalArgumentException("PKCE S256 is required");
        }
        validateScopes(requestedScopes);
    }

    public void validateAuthorizationCodeClient(String plainSecret, Set<String> scopes) {
        ensureEnabled();
        ensureGrantType("authorization_code");
        if (clientType == ClientType.CONFIDENTIAL && !matchesSecret(plainSecret)) {
            throw new IllegalArgumentException("invalid client secret");
        }
        validateScopes(scopes);
    }

    public void validateClientCredentials(String plainSecret, Set<String> scopes) {
        ensureEnabled();
        ensureGrantType("client_credentials");
        if (clientType != ClientType.CONFIDENTIAL) {
            throw new IllegalArgumentException("client_credentials requires a confidential client");
        }
        if (!matchesSecret(plainSecret)) {
            throw new IllegalArgumentException("invalid client secret");
        }
        validateScopes(scopes);
    }

    public boolean matchesSecret(String plainSecret) {
        if (blank(secretHash) || plainSecret == null) {
            return false;
        }
        byte[] expected = secretHash.getBytes(StandardCharsets.US_ASCII);
        byte[] actual = hashSecret(plainSecret).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    public static String hashSecret(String plainSecret) {
        if (blank(plainSecret)) {
            throw new IllegalArgumentException("client secret is required");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(plainSecret.getBytes(StandardCharsets.UTF_8));
            return "SHA256:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private void ensureEnabled() {
        if (status != ENABLED) {
            throw new IllegalStateException("OAuth client is disabled");
        }
    }

    private void ensureGrantType(String grantType) {
        if (!grantTypes.contains(grantType)) {
            throw new IllegalArgumentException("grant_type is not allowed for this client");
        }
    }

    private void validateScopes(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("scope is required");
        }
        if (!allowedScopes.containsAll(scopes)) {
            throw new IllegalArgumentException("requested scope exceeds client allowance");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public String clientId() {
        return clientId;
    }

    public String appCode() {
        return appCode;
    }

    public int accessTtlSeconds() {
        return accessTtlSeconds;
    }

    public int idTokenTtlSeconds() {
        return idTokenTtlSeconds;
    }

    public enum ClientType {
        /** Public client without a client secret. */
        PUBLIC,
        /** Confidential client authenticated by a secret. */
        CONFIDENTIAL
    }
}
