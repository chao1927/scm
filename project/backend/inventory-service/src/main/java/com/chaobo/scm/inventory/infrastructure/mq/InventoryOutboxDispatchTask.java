package com.chaobo.scm.inventory.infrastructure.mq;

import com.chaobo.scm.inventory.application.InventoryOutboxDispatchApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 库存 Outbox 定时投递任务。
 *
 * <p>任务只在非测试环境运行，并且依赖真实 RocketMQ 投递服务，不存在运行时降级分支。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class InventoryOutboxDispatchTask {

    private final InventoryOutboxDispatchApplicationService service;
    private final int batchSize;
    private final int maxRetries;

    public InventoryOutboxDispatchTask(
            InventoryOutboxDispatchApplicationService service,
            @Value("${scm.outbox.batch-size:100}") int batchSize,
            @Value("${scm.outbox.max-retries:16}") int maxRetries) {
        this.service = service;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    /**
     * 周期性扫描并投递待发送事件。
     */
    @Scheduled(fixedDelayString = "${scm.outbox.fixed-delay:1000}")
    public void publishPendingEvents() {
        service.dispatch(batchSize, maxRetries);
    }
}
