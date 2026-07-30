package com.chaobo.scm.supplier.application.operations.export;

import com.chaobo.scm.supplier.infrastructure.persistence.operations.SupplierOperationsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * 定时扫描并分派供应商导出任务。
 *
 * <p>扫描同时包含待处理、到期失败任务和超时处理中任务；乐观锁保证多实例调度时只有一个节点取得执行权。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SupplierExportDispatchTask {

    private final SupplierOperationsMapper mapper;
    private final SupplierExportProcessor processor;
    private final int batchSize;
    private final int maxRetries;
    private final int processingTimeoutSeconds;

    public SupplierExportDispatchTask(SupplierOperationsMapper mapper, SupplierExportProcessor processor,
                                      @Value("${scm.supplier.export.batch-size:10}") int batchSize,
                                      @Value("${scm.supplier.export.max-retries:5}") int maxRetries,
                                      @Value("${scm.supplier.export.processing-timeout-seconds:600}")
                                      int processingTimeoutSeconds) {
        this.mapper = mapper;
        this.processor = processor;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${scm.supplier.export.fixed-delay:3000}")
    public void dispatch() {
        mapper.claimableExports(maxRetries, OffsetDateTime.now().minusSeconds(processingTimeoutSeconds), batchSize)
                .forEach(processor::process);
    }
}
