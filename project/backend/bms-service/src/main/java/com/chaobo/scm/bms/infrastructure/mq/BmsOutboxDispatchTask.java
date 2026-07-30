package com.chaobo.scm.bms.infrastructure.mq;

import com.chaobo.scm.bms.application.outbox.BmsOutboxDispatchApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * BMS Outbox 定时调度。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class BmsOutboxDispatchTask {

    private final BmsOutboxDispatchApplicationService service;
    private final int batchSize;
    private final int maxRetries;

    public BmsOutboxDispatchTask(
            BmsOutboxDispatchApplicationService service,
            @Value("${scm.outbox.batch-size:100}") int batchSize,
            @Value("${scm.outbox.max-retries:16}") int maxRetries) {
        this.service = service;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    @Scheduled(fixedDelayString = "${scm.outbox.fixed-delay:1000}")
    public void dispatch() {
        service.dispatch(batchSize, maxRetries);
    }
}
