package com.chaobo.scm.bms.infrastructure.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * BMS 外部财务系统 HTTP 客户端配置。
 *
 * <p>Spring Boot 4 的基础 Web Starter 不保证注册 {@link RestClient.Builder}。
 * 本配置提供可被应用覆盖的默认 Builder，使 ERP、税控和支付防腐层可以启动，
 * 同时保留部署环境注入统一超时、代理和观测拦截器的扩展点。
 */
@Configuration(proxyBeanMethods = false)
public class BmsHttpClientConfiguration {

    /**
     * 创建默认 RestClient Builder；已有自定义 Bean 时不参与注册。
     *
     * @return 外部财务接口客户端 Builder
     */
    @Bean
    @ConditionalOnMissingBean(RestClient.Builder.class)
    RestClient.Builder bmsRestClientBuilder() {
        return RestClient.builder();
    }
}
