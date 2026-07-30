package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.infrastructure.persistence.OmsFulfillmentMetricsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 扫描待处理、到期重试和超时处理中的 OMS 指标导出任务。 */
@Component
public class OmsMetricExportDispatchTask {

    private final OmsFulfillmentMetricsMapper mapper;
    private final OmsMetricExportProcessor processor;
    private final int batchSize;
    private final int maxRetries;
    private final int processingTimeoutSeconds;

    public OmsMetricExportDispatchTask(
            OmsFulfillmentMetricsMapper mapper,
            OmsMetricExportProcessor processor,
            @Value("${scm.oms.metric-export.batch-size:10}") int batchSize,
            @Value("${scm.oms.metric-export.max-retries:5}") int maxRetries,
            @Value("${scm.oms.metric-export.processing-timeout-seconds:600}")
            int processingTimeoutSeconds) {
        this.mapper = mapper;
        this.processor = processor;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${scm.oms.metric-export.fixed-delay:3000}")
    public void dispatch() {
        mapper.claimableExports(maxRetries,
                        LocalDateTime.now().minusSeconds(processingTimeoutSeconds), batchSize)
                .forEach(processor::process);
    }
}
