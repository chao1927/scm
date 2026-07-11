package com.chaobo.scm.supplier.infrastructure.mq;
import com.chaobo.scm.supplier.application.outbox.*;import jakarta.annotation.PreDestroy;import org.apache.rocketmq.client.apis.*;import org.apache.rocketmq.client.apis.producer.Producer;import org.springframework.beans.factory.annotation.Value;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Component;import java.nio.charset.StandardCharsets;
@Component @ConditionalOnProperty(name="scm.rocketmq.enabled",havingValue="true")public class RocketMqMessageBrokerAdapter implements MessageBrokerPort{
 private final ClientServiceProvider provider=ClientServiceProvider.loadService();private final Producer producer;private final String topic;
 public RocketMqMessageBrokerAdapter(@Value("${scm.rocketmq.endpoints}")String endpoints,@Value("${scm.rocketmq.topic:supplier-domain-event}")String topic)throws ClientException{this.topic=topic;var config=ClientConfiguration.newBuilder().setEndpoints(endpoints).build();this.producer=provider.newProducerBuilder().setClientConfiguration(config).setTopics(topic).build();}
 public void publish(OutboxMessage m)throws ClientException{var message=provider.newMessageBuilder().setTopic(topic).setTag(m.eventType()).setKeys(m.eventCode()).setBody(m.payloadJson().getBytes(StandardCharsets.UTF_8)).build();producer.send(message);}
 @PreDestroy public void close()throws Exception{producer.close();}
}
