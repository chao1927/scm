package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.infrastructure.persistence.OmsFulfillmentMetricsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** 在事务外生成 CSV 并写入持久化对象存储。 */
@Service
public class OmsMetricExportProcessor {

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";

    private final OmsFulfillmentMetricsApplicationService metricsService;
    private final OmsMetricExportLifecycle lifecycle;
    private final OmsMetricExportObjectStoragePort storage;
    private final OmsMetricCsvWriter csvWriter = new OmsMetricCsvWriter();
    private final int maxRetries;
    private final int retryDelaySeconds;
    private final int maxRows;

    public OmsMetricExportProcessor(
            OmsFulfillmentMetricsApplicationService metricsService,
            OmsMetricExportLifecycle lifecycle,
            OmsMetricExportObjectStoragePort storage,
            @Value("${scm.oms.metric-export.max-retries:5}") int maxRetries,
            @Value("${scm.oms.metric-export.retry-delay-seconds:60}")
            int retryDelaySeconds,
            @Value("${scm.oms.metric-export.max-rows:100000}") int maxRows) {
        this.metricsService = metricsService;
        this.lifecycle = lifecycle;
        this.storage = storage;
        this.maxRetries = maxRetries;
        this.retryDelaySeconds = retryDelaySeconds;
        this.maxRows = maxRows;
    }

    public void process(OmsFulfillmentMetricsMapper.ExportTaskRow task) {
        if (!lifecycle.claim(task.id(), task.version())) {
            return;
        }
        long processingVersion = task.version() + 1;
        try {
            var result = metricsService.calculate(
                    new OmsFulfillmentMetricsApplicationService.Period(
                            task.periodStart(), task.periodEnd()),
                    new OmsFulfillmentMetricsApplicationService.ScopeSnapshot(
                            task.organizationScope(), task.ownerScope(), task.warehouseScope()));
            if (result.rows().size() > maxRows) {
                throw new IllegalArgumentException("导出行数超过上限 " + maxRows);
            }
            byte[] content = csvWriter.write(result);
            String fileName = "oms-fulfillment-metrics-" + task.exportNo() + ".csv";
            String objectKey = "oms-metric-exports/" + task.exportNo() + '/' + fileName;
            var stored = storage.store(objectKey, content, CSV_CONTENT_TYPE);
            lifecycle.complete(task.id(), processingVersion, stored,
                    fileName, result.rows().size());
        } catch (RuntimeException exception) {
            lifecycle.fail(task.id(), processingVersion, maxRetries,
                    LocalDateTime.now().plusSeconds(retryDelaySeconds),
                    failureReason(exception));
        }
    }

    private String failureReason(RuntimeException exception) {
        String message = exception.getMessage();
        String reason = exception.getClass().getSimpleName()
                + (message == null || message.isBlank() ? "" : ": " + message);
        return reason.length() <= 500 ? reason : reason.substring(0, 500);
    }
}
