package org.scm.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventPublisher {

//    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void publish(DomainEvent event) {
        rocketMQTemplate.convertAndSend(event.topic(), event);
        log.info("Event published: {} -> {}", event.topic(), event);
    }

}
