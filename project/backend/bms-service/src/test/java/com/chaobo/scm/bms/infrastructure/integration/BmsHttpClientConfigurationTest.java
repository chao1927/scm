package com.chaobo.scm.bms.infrastructure.integration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class BmsHttpClientConfigurationTest {

    @Test
    void shouldProvideRestClientBuilderWhenBootDoesNot() {
        try (var context = new AnnotationConfigApplicationContext(
                BmsHttpClientConfiguration.class)) {
            assertThat(context.getBean(RestClient.Builder.class)).isNotNull();
        }
    }
}
