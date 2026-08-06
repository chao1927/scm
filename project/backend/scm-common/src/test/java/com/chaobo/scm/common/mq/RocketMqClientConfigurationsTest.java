package com.chaobo.scm.common.mq;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RocketMqClientConfigurationsTest {

    @AfterEach
    void clearSslOverride() {
        System.clearProperty("scm.rocketmq.ssl-enabled");
    }

    @Test
    void shouldKeepTlsEnabledByDefault() {
        assertThat(RocketMqClientConfigurations.create("broker.example:8081")
                .isSslEnabled()).isTrue();
    }

    @Test
    void shouldAllowExplicitPlaintextForLocalDockerProxy() {
        System.setProperty("scm.rocketmq.ssl-enabled", "false");

        assertThat(RocketMqClientConfigurations.create("127.0.0.1:8081")
                .isSslEnabled()).isFalse();
    }
}
