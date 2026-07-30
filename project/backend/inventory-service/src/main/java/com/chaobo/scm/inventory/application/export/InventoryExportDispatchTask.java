package com.chaobo.scm.inventory.application.export;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 多实例安全的库存导出任务调度器。
 *
 * @author SCM Team
 */
@Component
public class InventoryExportDispatchTask {

    private final InventoryExportStorePort store;
    private final InventoryExportProcessor processor;
    private final int batchSize;
    private final int maxRetries;
    private final int timeoutSeconds;

    public InventoryExportDispatchTask(
            InventoryExportStorePort store,
            InventoryExportProcessor processor,
            @Value("${scm.inventory.export.batch-size:10}") int batchSize,
            @Value("${scm.inventory.export.max-retries:5}") int maxRetries,
            @Value("${scm.inventory.export.processing-timeout-seconds:600}") int timeoutSeconds) {
        this.store = store;
        this.processor = processor;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${scm.inventory.export.fixed-delay:3000}")
    public void dispatch() {
        store.claimable(
                        maxRetries,
                        LocalDateTime.now().minusSeconds(timeoutSeconds),
                        batchSize)
                .forEach(processor::process);
    }
}
