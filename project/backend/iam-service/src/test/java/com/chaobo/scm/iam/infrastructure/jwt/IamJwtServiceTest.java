package com.chaobo.scm.iam.infrastructure.jwt;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IamJwtServiceTest。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class IamJwtServiceTest {

    @Test
    void issuesWithActiveKidAndAcceptsPreviousOnlyInsideRotationWindow() {
        Instant now = Instant.parse("2026-07-30T00:00:00Z");
        IamJwtService oldIssuer = new IamJwtService(
                "old",
                "01234567890123456789012345678901",
                Map.of(),
                now::getEpochSecond);
        String oldToken = oldIssuer.issue(new IamJwtService.TokenClaims(
                "1", "admin", "IAM", "old-jti", "ACCESS",
                now.getEpochSecond(), now.plusSeconds(600).getEpochSecond()));
        IamJwtService rotating = new IamJwtService(
                "new",
                "abcdefghijklmnopqrstuvwxyzABCDEF",
                Map.of("old", new IamJwtService.VerificationKey(
                        "01234567890123456789012345678901",
                        now.plusSeconds(300).getEpochSecond())),
                now::getEpochSecond);

        assertThat(rotating.keyId(oldToken)).isEqualTo("old");
        assertThat(rotating.verify(oldToken).jti()).isEqualTo("old-jti");

        IamJwtService expiredWindow = new IamJwtService(
                "new",
                "abcdefghijklmnopqrstuvwxyzABCDEF",
                Map.of("old", new IamJwtService.VerificationKey(
                        "01234567890123456789012345678901",
                        now.minusSeconds(1).getEpochSecond())),
                now::getEpochSecond);
        assertThatThrownBy(() -> expiredWindow.verify(oldToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("verification window");
    }

    @Test
    void rejectsUnknownKidInsteadOfTryingEverySecret() {
        Instant now = Instant.parse("2026-07-30T00:00:00Z");
        IamJwtService issuer = new IamJwtService(
                "unknown",
                "01234567890123456789012345678901",
                Map.of(),
                now::getEpochSecond);
        String token = issuer.issue(new IamJwtService.TokenClaims(
                "1", "admin", "IAM", "jti", "ACCESS",
                now.getEpochSecond(), now.plusSeconds(60).getEpochSecond()));
        IamJwtService verifier = new IamJwtService(
                "active",
                "abcdefghijklmnopqrstuvwxyzABCDEF",
                Map.of(),
                now::getEpochSecond);

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown jwt kid");
    }

    @Test
    void rejectsTokenWhoseUnverifiedHeaderClaimsUnexpectedAlgorithm() {
        Instant now = Instant.parse("2026-07-30T00:00:00Z");
        IamJwtService service = new IamJwtService(
                "active",
                "01234567890123456789012345678901",
                Map.of(),
                now::getEpochSecond);
        String valid = service.issue(new IamJwtService.TokenClaims(
                "1", "admin", "IAM", "jti", "ACCESS",
                now.getEpochSecond(), now.plusSeconds(60).getEpochSecond()));
        String forgedHeader = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"alg\":\"none\",\"typ\":\"JWT\",\"kid\":\"active\"}"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String[] parts = valid.split("\\.");
        String signingInput = forgedHeader + "." + parts[1];
        String forged = signingInput + "." + hmac(signingInput,
                "01234567890123456789012345678901");

        assertThatThrownBy(() -> service.verify(forged))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("algorithm");
    }

    private static String hmac(String input, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                    mac.doFinal(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code signedJwtCanBeVerifiedAndTamperingIsRejected}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void signedJwtCanBeVerifiedAndTamperingIsRejected() {
        IamJwtService service = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();
        String token = service.issue(new IamJwtService.TokenClaims("1001", "admin", "IAM", "AT-1", "ACCESS", now, now + 60));
        IamJwtService.TokenClaims claims = service.verify(token);
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(claims.username()).isEqualTo("admin");
        assertThatThrownBy(() -> service.verify(tampered)).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code signedJwtCarriesPermissionAndDataScopeClaims}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void signedJwtCarriesPermissionAndDataScopeClaims() {
        IamJwtService service = new IamJwtService("01234567890123456789012345678901");
        long now = Instant.now().getEpochSecond();
        String token = service.issue(new IamJwtService.TokenClaims("1001", "buyer", "PURCHASE", "AT-2", "ACCESS", now, now + 60, Set.of("purchase:po:read", "purchase:po:create"), Map.of("PURCHASE_ORG", Set.of("ORG-1", "ORG-2"))));
        IamJwtService.TokenClaims claims = service.verify(token);
        assertThat(claims.permissions()).containsExactlyInAnyOrder("purchase:po:read", "purchase:po:create");
        assertThat(claims.dataScopes().get("PURCHASE_ORG")).containsExactlyInAnyOrder("ORG-1", "ORG-2");
    }

    /**
     * 处理当前类型职责中的操作 {@code weakSigningSecretIsRejected}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void weakSigningSecretIsRejected() {
        assertThatThrownBy(() -> new IamJwtService("short-secret")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("32");
    }
}
