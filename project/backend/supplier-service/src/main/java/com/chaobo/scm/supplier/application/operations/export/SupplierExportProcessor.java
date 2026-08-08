package com.chaobo.scm.supplier.application.operations.export;

import com.chaobo.scm.common.logging.ScmLogContext;
import com.chaobo.scm.supplier.application.operations.OperationViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * 编排单个供应商导出任务。
 *
 * <p>该服务本身不声明事务。它先通过生命周期端口短事务抢占任务，再在事务外完成查询、CSV 编码和对象存储，
 * 最后以乐观锁短事务完成或失败任务。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierExportProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SupplierExportProcessor.class);

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private final SupplierExportTaskLifecyclePort lifecycle;
    private final SupplierExportDataPort data;
    private final SupplierExportObjectStoragePort storage;
    private final SupplierCsvWriter csv;
    private final int maxRows;
    private final int retryDelaySeconds;

    @Autowired
    public SupplierExportProcessor(SupplierExportTaskLifecyclePort lifecycle, SupplierExportDataPort data,
                                   SupplierExportObjectStoragePort storage,
                                   @Value("${scm.supplier.export.max-rows:10000}") int maxRows,
                                   @Value("${scm.supplier.export.retry-delay-seconds:60}") int retryDelaySeconds) {
        this(lifecycle, data, storage, new SupplierCsvWriter(), maxRows, retryDelaySeconds);
    }

    SupplierExportProcessor(SupplierExportTaskLifecyclePort lifecycle, SupplierExportDataPort data,
                            SupplierExportObjectStoragePort storage, SupplierCsvWriter csv,
                            int maxRows, int retryDelaySeconds) {
        this.lifecycle = lifecycle;
        this.data = data;
        this.storage = storage;
        this.csv = csv;
        this.maxRows = maxRows;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    /**
     * 尝试处理任务；任务已被其他节点抢占时直接返回。
     */
    public void process(OperationViews.ExportTask task) {
        if (!lifecycle.claim(task.id(), task.version())) {
            return;
        }
        int processingVersion = task.version() + 1;
        try (ScmLogContext ignored = ScmLogContext.openSystem(Long.toString(task.id()))) {
            var rows = data.load(task.exportType(), task.supplierId(), task.queryJson(), maxRows);
            byte[] content = csv.write(SupplierExportDefinitions.columnsFor(task.exportType()), rows);
            String fileName = "supplier-" + task.exportType().toLowerCase(Locale.ROOT) + '-' + task.id() + ".csv";
            String objectKey = "supplier-exports/" + task.id() + '/' + fileName;
            var stored = storage.store(objectKey, content, CSV_CONTENT_TYPE);
            lifecycle.complete(task.id(), processingVersion, stored, fileName,
                    "/api/supplier/v1/operations/exports/" + task.id() + "/file");
            LOG.info("event=batch_task operation=supplier_export result=SUCCESS taskId={} exportType={}",
                    task.id(), task.exportType());
        } catch (RuntimeException exception) {
            lifecycle.fail(task.id(), processingVersion, failureReason(exception),
                    OffsetDateTime.now().plusSeconds(retryDelaySeconds));
            try (ScmLogContext ignored = ScmLogContext.openSystem(Long.toString(task.id()))) {
                LOG.error("event=batch_task operation=supplier_export result=FAILURE taskId={} exportType={}",
                        task.id(), task.exportType(), exception);
            }
        }
    }

    private String failureReason(RuntimeException exception) {
        String message = exception.getMessage();
        String reason = exception.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
        return reason.length() <= 500 ? reason : reason.substring(0, 500);
    }
}
