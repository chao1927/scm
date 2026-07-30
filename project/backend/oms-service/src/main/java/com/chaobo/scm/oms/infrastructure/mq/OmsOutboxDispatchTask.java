package com.chaobo.scm.oms.infrastructure.mq;

import com.chaobo.scm.oms.application.OmsOutboxDispatchApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 周期扫描并投递 OMS Outbox 的调度任务。
 */
@Component
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class OmsOutboxDispatchTask {

    private final OmsOutboxDispatchApplicationService service;
    private final int batchSize;

    public OmsOutboxDispatchTask(
            OmsOutboxDispatchApplicationService service,
            @Value("${scm.rocketmq.outbox.batch-size:50}") int batchSize) {
        this.service = service;
        this.batchSize = batchSize;
    }

    /**
     * 执行一次定时投递。
     */
    @Scheduled(fixedDelayString =
            "${scm.rocketmq.outbox.fixed-delay-ms:1000}")
    public void dispatch() {
        service.dispatch(batchSize);
    }
}
