package com.chaobo.scm.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(ScmSecurityProperties.class)
public class ScmSecurityConfiguration {
    @Bean
    JwtDecoder scmJwtDecoder(ScmSecurityProperties properties) {
        return NimbusJwtDecoder.withSecretKey(properties.secretKey()).build();
    }

    @Bean
    SecurityFilterChain scmSecurityFilterChain(HttpSecurity http, ScmSecurityProperties properties) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().access((authentication, context) -> {
                            var auth = authentication.get();
                            if (auth == null || !auth.isAuthenticated()) {
                                return new AuthorizationDecision(false);
                            }
                            String namespace = properties.getPermissionNamespace();
                            if (namespace.isBlank()) {
                                return new AuthorizationDecision(true);
                            }
                            String prefix = namespace + ":";
                            boolean allowed = auth.getAuthorities().stream()
                                    .map(authority -> authority.getAuthority())
                                    .anyMatch(value -> value.equals("*") || value.startsWith(prefix));
                            return new AuthorizationDecision(allowed);
                        }))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new ScmJwtAuthenticationConverter())))
                .build();
    }
}
