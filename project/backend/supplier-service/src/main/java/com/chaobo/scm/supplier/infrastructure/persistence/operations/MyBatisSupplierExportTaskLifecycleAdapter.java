package com.chaobo.scm.supplier.infrastructure.persistence.operations;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.operations.export.SupplierExportObjectStoragePort;
import com.chaobo.scm.supplier.application.operations.export.SupplierExportTaskLifecyclePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 使用 MyBatis 和乐观锁实现导出任务的短事务状态迁移。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class MyBatisSupplierExportTaskLifecycleAdapter implements SupplierExportTaskLifecyclePort {

    private final SupplierOperationsMapper mapper;

    public MyBatisSupplierExportTaskLifecycleAdapter(SupplierOperationsMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claim(long taskId, int version) {
        return mapper.claimExport(taskId, version) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(long taskId, int processingVersion, SupplierExportObjectStoragePort.StoredObject stored,
                         String fileName, String downloadUrl) {
        if (mapper.completeExport(taskId, processingVersion, downloadUrl, stored.objectKey(), fileName,
                stored.contentType(), stored.size()) != 1) {
            throw versionConflict();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fail(long taskId, int processingVersion, String reason, OffsetDateTime retryAt) {
        if (mapper.failExport(taskId, processingVersion, reason, retryAt) != 1) {
            throw versionConflict();
        }
    }

    private BusinessException versionConflict() {
        return new BusinessException(ErrorCode.VERSION_CONFLICT, "导出任务状态或版本已变更");
    }
}
