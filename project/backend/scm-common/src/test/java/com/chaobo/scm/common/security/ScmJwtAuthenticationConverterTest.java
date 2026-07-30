package com.chaobo.scm.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ScmJwtAuthenticationConverterTest。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ScmJwtAuthenticationConverterTest {

    /**
     * 处理当前类型职责中的操作 {@code convertsPermissionsAndDataScopesFromVerifiedJwt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void convertsPermissionsAndDataScopesFromVerifiedJwt() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("42").claim("username", "operator").claim("app", "SCM").claim("permissions", List.of("purchase:po:read", "wms:*")).claim("data_scopes", Map.of("WAREHOUSE", List.of("WH01"))).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        var authentication = new ScmJwtAuthenticationConverter().convert(jwt);
        ScmAccessContext context = (ScmAccessContext) authentication.getDetails();
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactlyInAnyOrder("purchase:po:read", "wms:*");
        assertThat(context.operatorId()).isEqualTo(42L);
        assertThat(context.dataScopes()).containsEntry("WAREHOUSE", java.util.Set.of("WH01"));
    }
}
