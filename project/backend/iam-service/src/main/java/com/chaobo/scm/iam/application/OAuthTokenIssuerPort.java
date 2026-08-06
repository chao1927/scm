package com.chaobo.scm.iam.application;

import java.util.Set;

/**
 * OAuth/OIDC 与现有 JWT/TokenCache 的集成端口。
 *
 * <p>B1 仅稳定授权协议与领域边界；具体签名、会话缓存和密钥轮换由集成阶段适配。
 *
 * @author chaobo
 */
@SuppressWarnings({"PMD.ClassNamingShouldBeCamelRule",
        "PMD.AbstractMethodOrInterfaceMethodMustUseJavadocRule"})
public interface OAuthTokenIssuerPort {

    IssuedTokens issueAuthorizationCodeTokens(AuthorizationCodeTokenCommand command);

    IssuedTokens issueClientCredentialsToken(ClientCredentialsTokenCommand command);

    default IssuedTokens issueRefreshTokenTokens(AuthorizationCodeTokenCommand command) {
        return issueAuthorizationCodeTokens(command);
    }

    UserInfo userInfo(String bearerToken);

    record AuthorizationCodeTokenCommand(
            long userId,
            String clientId,
            String appCode,
            Set<String> scopes,
            String nonce,
            int accessTtlSeconds,
            int idTokenTtlSeconds
    ) {
    }

    record ClientCredentialsTokenCommand(
            String clientId,
            String appCode,
            Set<String> scopes,
            int accessTtlSeconds
    ) {
    }

    record IssuedTokens(
            String accessToken,
            String refreshToken,
            String idToken,
            String tokenType,
            int expiresIn,
            Set<String> scopes
    ) {
    }

    record UserInfo(String subject, String username, String name, Set<String> scopes) {
    }
}
