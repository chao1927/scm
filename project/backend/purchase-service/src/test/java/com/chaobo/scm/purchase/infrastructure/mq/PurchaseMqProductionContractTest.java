package com.chaobo.scm.purchase.infrastructure.mq;

import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 采购生产环境真实 RocketMQ Bean 与配置契约测试。
 */
class PurchaseMqProductionContractTest {

    @Test
    void shouldExposeRealConditionalProducerAndConsumerBeans() {
        assertTrue(RocketMqMessageBrokerAdapter.class.isAnnotationPresent(Component.class));
        assertTrue(RocketMqPurchaseExternalEventConsumer.class.isAnnotationPresent(Component.class));
        assertTrue(MessageListener.class.isAssignableFrom(RocketMqPurchaseExternalEventConsumer.class));

        var producerCondition = RocketMqMessageBrokerAdapter.class
                .getAnnotation(ConditionalOnProperty.class);
        var consumerCondition = RocketMqPurchaseExternalEventConsumer.class
                .getAnnotation(ConditionalOnProperty.class);
        assertEquals("scm.rocketmq.enabled", producerCondition.name()[0]);
        assertEquals("true", producerCondition.havingValue());
        assertEquals("scm.rocketmq.enabled", consumerCondition.name()[0]);
        assertEquals("true", consumerCondition.havingValue());
    }

    @Test
    void shouldEnableRealBrokerAndDeclareExternalTopicsInProd() throws Exception {
        var resource = PurchaseMqProductionContractTest.class.getClassLoader()
                .getResourceAsStream("application-prod.yml");
        assertTrue(resource != null);
        var yaml = new String(resource.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(yaml.contains("enabled: ${SCM_ROCKETMQ_ENABLED:true}"));
        assertTrue(yaml.contains("external-consumer:"));
        assertTrue(yaml.contains("supplier-domain-event,wms-domain-event,tms-domain-event,bms-domain-event"));
        assertTrue(yaml.contains("purchase-business-event-consumer"));
    }
}
