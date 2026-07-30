package com.chaobo.scm.inventory.application.export;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存导出任务持久化及短事务生命周期端口。
 *
 * @author SCM Team
 */
public interface InventoryExportStorePort {

    InventoryExportTask create(CreateTask task);

    InventoryExportTask find(String taskNo);

    List<InventoryExportTask> list(long createdBy, Integer status, int offset, int limit);

    List<InventoryExportTask> claimable(
            int maxRetries, LocalDateTime staleBefore, int limit);

    boolean claim(long id, int version);

    boolean complete(
            long id,
            int version,
            InventoryExportObjectStoragePort.StoredObject object,
            String fileName);

    boolean fail(long id, int version, String reason, LocalDateTime retryAt);

    boolean retry(String taskNo, long createdBy, int version);

    /**
     * 创建任务所需的不可变快照。
     */
    record CreateTask(
            String taskNo,
            String exportType,
            String queryJson,
            String ownerScopeJson,
            String warehouseScopeJson,
            long createdBy,
            String idempotencyKey,
            String requestFingerprint) {
    }
}
