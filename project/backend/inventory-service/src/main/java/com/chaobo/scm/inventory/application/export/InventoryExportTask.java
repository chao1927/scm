package com.chaobo.scm.inventory.application.export;

import java.time.LocalDateTime;

/**
 * 库存异步导出任务只读视图。
 *
 * @author SCM Team
 */
public record InventoryExportTask(
        long id,
        String taskNo,
        String exportType,
        String queryJson,
        String ownerScopeJson,
        String warehouseScopeJson,
        long createdBy,
        int status,
        int retryCount,
        LocalDateTime nextRetryAt,
        String objectKey,
        String fileName,
        String contentType,
        Long fileSize,
        String lastError,
        int version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
