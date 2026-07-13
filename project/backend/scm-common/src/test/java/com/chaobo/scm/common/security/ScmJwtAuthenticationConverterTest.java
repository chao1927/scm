package com.chaobo.scm.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScmJwtAuthenticationConverterTest {
    @Test
    void convertsPermissionsAndDataScopesFromVerifiedJwt() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .claim("username", "operator")
                .claim("app", "SCM")
                .claim("permissions", List.of("purchase:po:read", "wms:*"))
                .claim("data_scopes", Map.of("WAREHOUSE", List.of("WH01")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        var authentication = new ScmJwtAuthenticationConverter().convert(jwt);
        ScmAccessContext context = (ScmAccessContext) authentication.getDetails();

        assertThat(authentication.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("purchase:po:read", "wms:*");
        assertThat(context.operatorId()).isEqualTo(42L);
        assertThat(context.dataScopes()).containsEntry("WAREHOUSE", java.util.Set.of("WH01"));
    }
}
