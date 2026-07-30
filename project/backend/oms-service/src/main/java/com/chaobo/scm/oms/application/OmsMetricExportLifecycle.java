package com.chaobo.scm.oms.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.oms.infrastructure.persistence.OmsFulfillmentMetricsMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 以独立短事务推进 OMS 履约指标导出任务。 */
@Component
public class OmsMetricExportLifecycle {

    private final OmsFulfillmentMetricsMapper mapper;

    public OmsMetricExportLifecycle(OmsFulfillmentMetricsMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public boolean claim(long id, long version) {
        return mapper.claimExport(id, version) == 1;
    }

    @Transactional
    public void complete(long id, long processingVersion,
                         OmsMetricExportObjectStoragePort.StoredObject stored,
                         String fileName, int recordCount) {
        if (mapper.completeExport(id, processingVersion, stored.objectKey(), fileName,
                stored.contentType(), stored.size(), recordCount) != 1) {
            throw conflict();
        }
    }

    @Transactional
    public void fail(long id, long processingVersion, int maxRetries,
                     LocalDateTime nextRetryAt, String lastError) {
        if (mapper.failExport(id, processingVersion, maxRetries,
                nextRetryAt, lastError) != 1) {
            throw conflict();
        }
    }

    private BusinessException conflict() {
        return new BusinessException(
                ErrorCode.VERSION_CONFLICT, "导出任务状态或版本已变更");
    }
}
