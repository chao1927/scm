package com.chaobo.scm.bms.application.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 外部财税支付任务调度器。
 *
 * @author SCM Team
 */
@Component
public class BmsExternalTaskDispatchJob {

    private final BmsExternalIntegrationApplicationService service;
    private final int batchSize;

    public BmsExternalTaskDispatchJob(
            BmsExternalIntegrationApplicationService service,
            @Value("${scm.bms.external.batch-size:50}") int batchSize) {
        this.service = service;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${scm.bms.external.fixed-delay:2000}")
    public void dispatch() {
        service.dispatch(batchSize);
    }
}
