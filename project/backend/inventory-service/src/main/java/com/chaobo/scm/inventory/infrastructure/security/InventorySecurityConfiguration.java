package com.chaobo.scm.inventory.infrastructure.security;

import com.chaobo.scm.common.security.ScmSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * InventorySecurityConfiguration。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。集中声明框架装配和运行配置，保持业务代码不感知基础设施初始化细节。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@Import(ScmSecurityConfiguration.class)
public class InventorySecurityConfiguration {
}
