package com.chaobo.scm.bms.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * BMS 报表导出任务调度器。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class BmsReportExportDispatchJob {

    private final BmsReportExportApplicationService service;
    private final int batchSize;

    /**
     * 创建导出任务调度器。
     */
    public BmsReportExportDispatchJob(
        BmsReportExportApplicationService service,
        @Value("${scm.bms.report.batch-size:20}") int batchSize) {
        this.service = service;
        this.batchSize = batchSize;
    }

    /**
     * 周期领取并生成报表。
     */
    @Scheduled(fixedDelayString = "${scm.bms.report.fixed-delay:2000}")
    public void dispatch() {
        service.dispatch(batchSize);
    }
}
