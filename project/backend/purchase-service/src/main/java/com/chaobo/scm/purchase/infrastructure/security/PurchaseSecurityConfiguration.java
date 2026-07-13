package com.chaobo.scm.purchase.infrastructure.security;

import com.chaobo.scm.common.security.ScmSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@Import(ScmSecurityConfiguration.class)
public class PurchaseSecurityConfiguration {
}
