package com.chaobo.scm.iam.infrastructure.jwt;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class IamJwtService {
    private static final int MINIMUM_SECRET_BYTES = 32;
    private final byte[] secret;
    private final ObjectMapper json = new ObjectMapper();

    public IamJwtService(String secret) {
        byte[] candidate = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (candidate.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException("IAM JWT signing secret must contain at least 32 bytes");
        }
        this.secret = candidate.clone();
    }

    public String issue(TokenClaims claims) {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", claims.subject());
        payload.put("username", claims.username());
        payload.put("app", claims.appCode());
        payload.put("jti", claims.jti());
        payload.put("type", claims.tokenType());
        payload.put("iat", claims.issuedAtEpochSecond());
        payload.put("exp", claims.expiresAtEpochSecond());
        payload.put("permissions", claims.permissions());
        payload.put("data_scopes", claims.dataScopes());
        try {
            String signingInput = base64Url(json.writeValueAsBytes(header)) + "."
                    + base64Url(json.writeValueAsBytes(payload));
            return signingInput + "." + sign(signingInput);
        } catch (JacksonException exception) {
            throw new IllegalStateException("jwt serialization failed", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public TokenClaims verify(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("invalid jwt format");
        }
        String signingInput = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(signingInput), parts[2])) {
            throw new IllegalArgumentException("invalid jwt signature");
        }
        try {
            Map<String, Object> values = json.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            long exp = number(values.get("exp"));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("jwt expired");
            }
            Set<String> permissions = stringSet(values.get("permissions"));
            Map<String, Set<String>> dataScopes = new LinkedHashMap<>();
            if (values.get("data_scopes") instanceof Map<?, ?> rawScopes) {
                rawScopes.forEach((key, value) -> dataScopes.put(String.valueOf(key), stringSet(value)));
            }
            return new TokenClaims(text(values, "sub"), text(values, "username"), text(values, "app"),
                    text(values, "jti"), text(values, "type"), number(values.get("iat")), exp,
                    permissions, dataScopes);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException invalid) {
                throw invalid;
            }
            throw new IllegalArgumentException("invalid jwt payload", exception);
        }
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return base64Url(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("jwt signing failed", exception);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }

    private static String text(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static long number(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static Set<String> stringSet(Object value) {
        if (!(value instanceof Iterable<?> values)) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        values.forEach(item -> result.add(String.valueOf(item)));
        return Set.copyOf(result);
    }

    public record TokenClaims(String subject, String username, String appCode, String jti, String tokenType,
                              long issuedAtEpochSecond, long expiresAtEpochSecond, Set<String> permissions,
                              Map<String, Set<String>> dataScopes) {
        public TokenClaims {
            permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
            if (dataScopes == null || dataScopes.isEmpty()) {
                dataScopes = Map.of();
            } else {
                Map<String, Set<String>> copy = new LinkedHashMap<>();
                dataScopes.forEach((key, values) -> copy.put(key, values == null ? Set.of() : Set.copyOf(values)));
                dataScopes = Map.copyOf(copy);
            }
        }

        public TokenClaims(String subject, String username, String appCode, String jti, String tokenType,
                           long issuedAtEpochSecond, long expiresAtEpochSecond) {
            this(subject, username, appCode, jti, tokenType, issuedAtEpochSecond, expiresAtEpochSecond,
                    Set.of(), Map.of());
        }
    }
}
