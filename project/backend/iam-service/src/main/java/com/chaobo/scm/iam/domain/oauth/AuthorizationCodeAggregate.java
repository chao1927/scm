package com.chaobo.scm.iam.domain.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 一次性授权码聚合。授权码绑定客户端、用户、回调地址、Scope、PKCE 与 nonce。
 *
 * @author chaobo
 */
public final class AuthorizationCodeAggregate {

    private static final Pattern VERIFIER_PATTERN = Pattern.compile("[A-Za-z0-9._~-]{43,128}");

    private final String codeHash;
    private final String clientId;
    private final long userId;
    private final String redirectUri;
    private final Set<String> scopes;
    private final String codeChallenge;
    private final String nonce;
    private final String requestId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private Instant consumedAt;

    private AuthorizationCodeAggregate(
            String codeHash,
            String clientId,
            long userId,
            String redirectUri,
            Set<String> scopes,
            String codeChallenge,
            String nonce,
            String requestId,
            Instant issuedAt,
            Instant expiresAt,
            Instant consumedAt
    ) {
        if (blank(codeHash) || blank(clientId) || blank(redirectUri) || blank(codeChallenge)
                || blank(requestId) || userId <= 0 || scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("authorization code bindings are required");
        }
        if (issuedAt == null || expiresAt == null || !expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("authorization code validity window is invalid");
        }
        this.codeHash = codeHash;
        this.clientId = clientId;
        this.userId = userId;
        this.redirectUri = redirectUri;
        this.scopes = Set.copyOf(scopes);
        this.codeChallenge = codeChallenge;
        this.nonce = nonce;
        this.requestId = requestId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
    }

    public static AuthorizationCodeAggregate issue(
            String codeHash,
            String clientId,
            long userId,
            String redirectUri,
            Set<String> scopes,
            String codeChallenge,
            String nonce,
            String requestId,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return new AuthorizationCodeAggregate(
                codeHash,
                clientId,
                userId,
                redirectUri,
                scopes,
                codeChallenge,
                nonce,
                requestId,
                issuedAt,
                expiresAt,
                null
        );
    }

    public static AuthorizationCodeAggregate restore(
            String codeHash,
            String clientId,
            long userId,
            String redirectUri,
            Set<String> scopes,
            String codeChallenge,
            String nonce,
            String requestId,
            Instant issuedAt,
            Instant expiresAt,
            Instant consumedAt
    ) {
        return new AuthorizationCodeAggregate(
                codeHash,
                clientId,
                userId,
                redirectUri,
                scopes,
                codeChallenge,
                nonce,
                requestId,
                issuedAt,
                expiresAt,
                consumedAt
        );
    }

    public void validateForConsumption(
            String requestedClientId,
            String requestedRedirectUri,
            String codeVerifier,
            Instant now
    ) {
        if (consumedAt != null) {
            throw new IllegalStateException("authorization code was already consumed");
        }
        if (!expiresAt.isAfter(now)) {
            throw new IllegalStateException("authorization code expired");
        }
        if (!clientId.equals(requestedClientId)) {
            throw new IllegalArgumentException("authorization code client mismatch");
        }
        if (!redirectUri.equals(requestedRedirectUri)) {
            throw new IllegalArgumentException("authorization code redirect_uri mismatch");
        }
        if (codeVerifier == null || !VERIFIER_PATTERN.matcher(codeVerifier).matches()) {
            throw new IllegalArgumentException("PKCE code_verifier is invalid");
        }
        byte[] expected = codeChallenge.getBytes(StandardCharsets.US_ASCII);
        byte[] actual = pkceChallenge(codeVerifier).getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new IllegalArgumentException("PKCE verification failed");
        }
    }

    public static String pkceChallenge(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public String codeHash() {
        return codeHash;
    }

    public String clientId() {
        return clientId;
    }

    public long userId() {
        return userId;
    }

    public String redirectUri() {
        return redirectUri;
    }

    public Set<String> scopes() {
        return scopes;
    }

    public String codeChallenge() {
        return codeChallenge;
    }

    public String nonce() {
        return nonce;
    }

    public String requestId() {
        return requestId;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant consumedAt() {
        return consumedAt;
    }
}
