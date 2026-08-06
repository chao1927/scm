package com.chaobo.scm.mdm.infrastructure.mq;

import com.chaobo.scm.mdm.application.outbox.MdmOutboxDispatchApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 主数据 Outbox 定时投递任务。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class MdmOutboxDispatchTask {

    private final MdmOutboxDispatchApplicationService service;
    private final int batchSize;
    private final int maxRetries;

    public MdmOutboxDispatchTask(
            MdmOutboxDispatchApplicationService service,
            @Value("${scm.outbox.batch-size:100}") int batchSize,
            @Value("${scm.outbox.max-retries:16}") int maxRetries) {
        this.service = service;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    /** 触发一批待投递事件。 */
    @Scheduled(fixedDelayString = "${scm.outbox.fixed-delay:1000}")
    public void dispatch() {
        service.dispatch(batchSize, maxRetries);
    }
}
