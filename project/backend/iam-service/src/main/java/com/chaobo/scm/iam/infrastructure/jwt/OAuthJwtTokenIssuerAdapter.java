package com.chaobo.scm.iam.infrastructure.jwt;

import com.chaobo.scm.iam.application.IamTokenClaimsProvider;
import com.chaobo.scm.iam.application.OAuthTokenIssuerPort;
import com.chaobo.scm.iam.infrastructure.persistence.IamMapper;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;

/** Issues signed OAuth/OIDC tokens through the IAM signing-key lifecycle. */
@Component
public class OAuthJwtTokenIssuerAdapter implements OAuthTokenIssuerPort {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final IamJwtService jwt;
    private final IamTokenClaimsProvider claimsProvider;
    private final LongFunction<String> usernameResolver;
    private final LongSupplier currentEpochSecond;

    public OAuthJwtTokenIssuerAdapter(IamJwtService jwt, IamTokenClaimsProvider claimsProvider,
                                      IamMapper iamMapper) {
        this(jwt, claimsProvider, userId -> {
            IamMapper.UserRow user = iamMapper.findUserById(userId);
            if (user == null || user.status() != 1) {
                throw new IllegalArgumentException("OAuth user is invalid or disabled");
            }
            return user.username();
        }, () -> Instant.now().getEpochSecond());
    }

    OAuthJwtTokenIssuerAdapter(IamJwtService jwt, IamTokenClaimsProvider claimsProvider,
                               LongFunction<String> usernameResolver, LongSupplier currentEpochSecond) {
        this.jwt = jwt;
        this.claimsProvider = claimsProvider;
        this.usernameResolver = usernameResolver;
        this.currentEpochSecond = currentEpochSecond;
    }

    @Override
    public IssuedTokens issueAuthorizationCodeTokens(AuthorizationCodeTokenCommand command) {
        long now = currentEpochSecond.getAsLong();
        String username = usernameResolver.apply(command.userId());
        IamTokenClaimsProvider.PermissionClaims claims = claimsProvider.claimsFor(command.userId());
        Set<String> accessPermissions = new LinkedHashSet<>(claims.permissions());
        accessPermissions.addAll(command.scopes());
        String access = jwt.issue(new IamJwtService.TokenClaims(String.valueOf(command.userId()), username,
                command.appCode(), UUID.randomUUID().toString(), "OAUTH_ACCESS", now,
                now + command.accessTtlSeconds(), accessPermissions, claims.dataScopes()));
        String idToken = command.scopes().contains("openid")
                ? jwt.issue(new IamJwtService.TokenClaims(String.valueOf(command.userId()), username,
                command.appCode(), command.nonce() == null ? UUID.randomUUID().toString() : command.nonce(),
                "OIDC_ID", now, now + command.idTokenTtlSeconds(),
                Set.of(), Map.of()))
                : null;
        return new IssuedTokens(access, randomOpaqueToken(), idToken, "Bearer",
                command.accessTtlSeconds(), command.scopes());
    }

    @Override
    public IssuedTokens issueClientCredentialsToken(ClientCredentialsTokenCommand command) {
        long now = currentEpochSecond.getAsLong();
        String access = jwt.issue(new IamJwtService.TokenClaims("client:" + command.clientId(),
                command.clientId(), command.appCode(), UUID.randomUUID().toString(), "OAUTH_CLIENT_ACCESS",
                now, now + command.accessTtlSeconds(), command.scopes(), Map.of()));
        return new IssuedTokens(access, null, null, "Bearer", command.accessTtlSeconds(), command.scopes());
    }

    @Override
    public UserInfo userInfo(String bearerToken) {
        IamJwtService.TokenClaims claims = jwt.verify(stripBearer(bearerToken));
        if (!"OAUTH_ACCESS".equals(claims.tokenType()) || claims.subject().startsWith("client:")) {
            throw new IllegalArgumentException("UserInfo requires a user access token");
        }
        return new UserInfo(claims.subject(), claims.username(), claims.username(), claims.permissions());
    }

    private static String stripBearer(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("bearer token is required");
        }
        return value.regionMatches(true, 0, "Bearer ", 0, 7) ? value.substring(7).trim() : value;
    }

    private static String randomOpaqueToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
