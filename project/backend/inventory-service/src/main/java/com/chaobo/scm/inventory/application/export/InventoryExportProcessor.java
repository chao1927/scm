package com.chaobo.scm.inventory.application.export;

import com.chaobo.scm.common.logging.ScmLogContext;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在短事务之外生成 CSV 并写入对象存储。
 *
 * @author SCM Team
 */
@Service
public class InventoryExportProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryExportProcessor.class);

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private final InventoryExportStorePort store;
    private final InventoryExportDataPort data;
    private final InventoryExportObjectStoragePort storage;
    private final int maxRows;
    private final int retryDelaySeconds;

    public InventoryExportProcessor(
            InventoryExportStorePort store,
            InventoryExportDataPort data,
            InventoryExportObjectStoragePort storage,
            @Value("${scm.inventory.export.max-rows:10000}") int maxRows,
            @Value("${scm.inventory.export.retry-delay-seconds:60}") int retryDelaySeconds) {
        this.store = store;
        this.data = data;
        this.storage = storage;
        this.maxRows = maxRows;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public void process(InventoryExportTask task) {
        if (!store.claim(task.id(), task.version())) {
            return;
        }
        int processingVersion = task.version() + 1;
        try (ScmLogContext ignored = ScmLogContext.openSystem(task.taskNo())) {
            byte[] content = new InventoryCsvWriter().write(
                    InventoryExportDefinitions.columnsFor(task.exportType()),
                    data.load(task, maxRows));
            String fileName = "inventory-"
                    + task.exportType().toLowerCase(Locale.ROOT)
                    + '-' + task.taskNo() + ".csv";
            String objectKey = "inventory-exports/" + task.taskNo() + '/' + fileName;
            InventoryExportObjectStoragePort.StoredObject object =
                    storage.store(objectKey, content, CSV_CONTENT_TYPE);
            if (!store.complete(task.id(), processingVersion, object, fileName)) {
                throw new IllegalStateException("导出任务完成状态冲突");
            }
            LOG.info("event=batch_task operation=inventory_export result=SUCCESS taskId={} taskNo={} exportType={}",
                    task.id(), task.taskNo(), task.exportType());
        } catch (RuntimeException exception) {
            store.fail(
                    task.id(),
                    processingVersion,
                    failureReason(exception),
                    LocalDateTime.now().plusSeconds(retryDelaySeconds));
            try (ScmLogContext ignored = ScmLogContext.openSystem(task.taskNo())) {
                LOG.error("event=batch_task operation=inventory_export result=FAILURE taskId={} taskNo={} exportType={}",
                        task.id(), task.taskNo(), task.exportType(), exception);
            }
        }
    }

    private static String failureReason(RuntimeException exception) {
        String message = exception.getMessage();
        String reason = exception.getClass().getSimpleName()
                + (message == null || message.isBlank() ? "" : ": " + message);
        return reason.length() <= 1000 ? reason : reason.substring(0, 1000);
    }
}
