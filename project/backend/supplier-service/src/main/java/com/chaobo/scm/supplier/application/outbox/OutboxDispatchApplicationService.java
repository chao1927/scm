package com.chaobo.scm.supplier.application.outbox;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.List;
@Service @ConditionalOnProperty(name="scm.rocketmq.enabled",havingValue="true") public class OutboxDispatchApplicationService{
 private final OutboxDispatchPort store;private final MessageBrokerPort broker;public OutboxDispatchApplicationService(OutboxDispatchPort store,MessageBrokerPort broker){this.store=store;this.broker=broker;}
 @Transactional public List<OutboxMessage> claim(int batch,int retries){return store.claim(batch,retries);}
 public void dispatch(OutboxMessage message){try{broker.publish(message);store.markPublished(message.eventId());}catch(Exception e){store.markFailed(message.eventId(),abbreviate(e.getMessage()));}}
 private String abbreviate(String value){if(value==null)return "未知投递异常";return value.length()<=1000?value:value.substring(0,1000);}
}
