package com.chaobo.scm.iam.infrastructure.mq;

import com.chaobo.scm.iam.application.outbox.IamOutboxDispatchApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * IAM Outbox 定时投递任务。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class IamOutboxDispatchTask {

    private final IamOutboxDispatchApplicationService service;
    private final int batchSize;
    private final int maxRetries;

    public IamOutboxDispatchTask(
            IamOutboxDispatchApplicationService service,
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
