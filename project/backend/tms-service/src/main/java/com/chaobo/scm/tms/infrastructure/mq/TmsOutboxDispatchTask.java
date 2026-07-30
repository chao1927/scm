package com.chaobo.scm.tms.infrastructure.mq;

import com.chaobo.scm.tms.application.outbox.TmsOutboxDispatchApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 周期扫描并投递 TMS Outbox。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class TmsOutboxDispatchTask {

    private final TmsOutboxDispatchApplicationService service;
    private final int batchSize;
    private final int maxRetries;

    public TmsOutboxDispatchTask(
            TmsOutboxDispatchApplicationService service,
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
