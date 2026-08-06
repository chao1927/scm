package com.chaobo.scm.iam.infrastructure.security;

import com.chaobo.scm.common.security.ScmSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * IAM HTTP 安全装配：复用公共 JWT 验签和权限命名空间规则。
 *
 * <p>匿名登录、MFA 完成、刷新与 OAuth 客户端端点由 IAM 的
 * {@code scm.security.public-paths} 精确白名单控制，其余请求必须通过认证和授权。
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@Import(ScmSecurityConfiguration.class)
public class IamSecurityConfiguration {
}
