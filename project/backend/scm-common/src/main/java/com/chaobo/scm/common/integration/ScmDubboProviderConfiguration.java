package com.chaobo.scm.common.integration;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 九服务共用的 Dubbo Provider 启动配置。
 *
 * <p>注册中心、端口和应用名均从环境或 Nacos 配置读取；本地契约测试可使用 {@code N/A}
 * 禁用注册，真实跨进程冒烟则使用 Nacos 地址。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
@DubboComponentScan(basePackages = "com.chaobo.scm")
public class ScmDubboProviderConfiguration {

    /** 创建与 Spring 服务同名的 Dubbo 应用标识。 */
    @Bean
    public ApplicationConfig scmDubboApplicationConfig(
            @Value("${spring.application.name}") String applicationName) {
        return new ApplicationConfig(applicationName);
    }

    /** 创建 Nacos 注册中心配置；{@code N/A} 表示仅本机暴露。 */
    @Bean
    public RegistryConfig scmDubboRegistryConfig(
            @Value("${scm.dubbo.registry-address:N/A}") String address) {
        return new RegistryConfig(address);
    }

    /** 创建 Triple 协议配置，端口为 -1 时由 Dubbo 选择可用端口。 */
    @Bean
    public ProtocolConfig scmDubboProtocolConfig(
            @Value("${scm.dubbo.protocol-port:-1}") int port) {
        return new ProtocolConfig("tri", port);
    }
}
