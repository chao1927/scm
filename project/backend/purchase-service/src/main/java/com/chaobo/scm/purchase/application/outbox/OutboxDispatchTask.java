package com.chaobo.scm.purchase.application.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class OutboxDispatchTask {
    private final OutboxDispatchApplicationService service;
    private final int batchSize;
    private final int maxRetries;

    public OutboxDispatchTask(OutboxDispatchApplicationService service,
                              @Value("${scm.outbox.batch-size:100}") int batchSize,
                              @Value("${scm.outbox.max-retries:16}") int maxRetries) {
        this.service = service;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    @Scheduled(fixedDelayString = "${scm.outbox.fixed-delay:1000}")
    public void publishPendingEvents() {
        service.claim(batchSize, maxRetries).forEach(service::dispatch);
    }
}
