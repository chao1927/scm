package com.chaobo.scm.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Map;

/**
 * ScmSecurityConfiguration。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。集中声明框架装配和运行配置，保持业务代码不感知基础设施初始化细节。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(ScmSecurityProperties.class)
public class ScmSecurityConfiguration {

    /**
     * 为存量 RocketMQ 信封编解码器提供 Jackson 2 兼容实例。
     *
     * <p>Spring Boot 4 默认自动装配 Jackson 3，但已发布的消息适配器仍使用
     * {@code com.fasterxml.jackson.databind.ObjectMapper}。两种类型包名不同，可并存且不改变消息契约。
     *
     * @return 已自动发现 Java Time 等模块的 Jackson 2 对象映射器
     */
    @Bean
    @ConditionalOnMissingBean(com.fasterxml.jackson.databind.ObjectMapper.class)
    com.fasterxml.jackson.databind.ObjectMapper scmLegacyJacksonObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
    }

    /**
     * 处理当前类型职责中的操作 {@code scmJwtDecoder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param properties 业务处理参数或成员，类型为 {@code ScmSecurityProperties}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code JwtDecoder}
     */
    @Bean
    JwtDecoder scmJwtDecoder(ScmSecurityProperties properties) {
        JwtDecoder active = NimbusJwtDecoder.withSecretKey(properties.activeSecretKey()).build();
        var previousKey = properties.previousSecretKey();
        if (previousKey.isEmpty()) {
            return new ScmKidAwareJwtDecoder(properties.getActiveKid(), active, Map.of());
        }
        JwtDecoder previous = NimbusJwtDecoder.withSecretKey(previousKey.orElseThrow()).build();
        return new ScmKidAwareJwtDecoder(properties.getActiveKid(), active,
                Map.of(properties.getPreviousKid(), new ScmKidAwareJwtDecoder.PreviousDecoder(
                        previous, properties.getPreviousValidUntilEpochSecond())));
    }

    /**
     * 处理当前类型职责中的操作 {@code scmSecurityFilterChain}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param http 业务处理参数或成员，类型为 {@code HttpSecurity}
     * @param properties 业务处理参数或成员，类型为 {@code ScmSecurityProperties}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SecurityFilterChain}
     */
    @Bean
    SecurityFilterChain scmSecurityFilterChain(HttpSecurity http, ScmSecurityProperties properties) throws Exception {
        return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/actuator/health", "/actuator/info").permitAll();
            if (!properties.getPublicPaths().isEmpty()) {
                authorize.requestMatchers(properties.getPublicPaths().toArray(String[]::new)).permitAll();
            }
            authorize.anyRequest().access((authentication, context) -> {
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
                        .anyMatch(value -> "*".equals(value) || value.startsWith(prefix));
                return new AuthorizationDecision(allowed);
            });
        }).oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
                jwt.jwtAuthenticationConverter(new ScmJwtAuthenticationConverter()))).build();
    }
}
