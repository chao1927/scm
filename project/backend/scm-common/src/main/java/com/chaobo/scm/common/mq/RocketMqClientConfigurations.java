package com.chaobo.scm.common.mq;

import org.apache.rocketmq.client.apis.ClientConfiguration;

/**
 * 统一创建 RocketMQ 5.x 客户端配置。
 *
 * <p>RocketMQ Java 客户端默认开启 TLS，而本地 Docker Proxy 默认提供明文 gRPC。
 * 生产环境保持安全的 TLS 默认值；本地或专有网络环境可通过 JVM 参数
 * {@code -Dscm.rocketmq.ssl-enabled=false} 或环境变量
 * {@code ROCKETMQ_SSL_ENABLED=false} 显式关闭。JVM 参数优先于环境变量。
 */
public final class RocketMqClientConfigurations {

    private static final String SSL_SYSTEM_PROPERTY =
            "scm.rocketmq.ssl-enabled";
    private static final String SSL_ENVIRONMENT_VARIABLE =
            "ROCKETMQ_SSL_ENABLED";

    private RocketMqClientConfigurations() {
    }

    /**
     * 按统一 TLS 策略创建客户端配置。
     *
     * @param endpoints RocketMQ Proxy gRPC 地址
     * @return 可直接交给生产者或消费者 Builder 的客户端配置
     */
    public static ClientConfiguration create(String endpoints) {
        return ClientConfiguration.newBuilder()
                .setEndpoints(endpoints)
                .enableSsl(sslEnabled())
                .build();
    }

    private static boolean sslEnabled() {
        String systemValue = System.getProperty(SSL_SYSTEM_PROPERTY);
        if (systemValue != null && !systemValue.isBlank()) {
            return Boolean.parseBoolean(systemValue);
        }
        String environmentValue = System.getenv(SSL_ENVIRONMENT_VARIABLE);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return Boolean.parseBoolean(environmentValue);
        }
        return true;
    }
}
