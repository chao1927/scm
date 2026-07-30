package com.chaobo.scm.supplier.application.operations.export;

import com.chaobo.scm.supplier.application.operations.OperationViews;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证导出处理器只编排短状态事务，并在外部 I/O 失败时留下可重试失败状态。
 */
class SupplierExportProcessorTest {

    @Test
    void completesClaimedTaskWithStoredObjectMetadata() {
        var lifecycle = new FakeLifecycle();
        var data = (SupplierExportDataPort) (type, supplierId, queryJson, maxRows) ->
                List.of(Map.of("warningId", 7L, "warningType", "DELAY"));
        var storage = new FakeStorage(false);
        var processor = new SupplierExportProcessor(lifecycle, data, storage, new SupplierCsvWriter(), 100, 60);

        processor.process(task(0, 1));

        assertThat(lifecycle.events).containsExactly("claim", "complete");
        assertThat(lifecycle.completedObjectKey).isEqualTo("supplier-exports/9001/supplier-warning-9001.csv");
        assertThat(storage.lastContent).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
    }

    @Test
    void recordsFailureReasonAndRetryTimeWhenStorageFails() {
        var lifecycle = new FakeLifecycle();
        var data = (SupplierExportDataPort) (type, supplierId, queryJson, maxRows) -> List.of(Map.of());
        var processor = new SupplierExportProcessor(lifecycle, data, new FakeStorage(true), new SupplierCsvWriter(), 100, 60);

        processor.process(task(2, 4));

        assertThat(lifecycle.events).containsExactly("claim", "fail");
        assertThat(lifecycle.failureReason).contains("storage unavailable");
        assertThat(lifecycle.nextRetryAt).isAfter(OffsetDateTime.now().plusSeconds(50));
    }

    private OperationViews.ExportTask task(int retryCount, int version) {
        return new OperationViews.ExportTask(9001L, "WARNING", 3001L, "{}", 1, null, null,
                null, null, null, null, retryCount, null, null, null,
                OffsetDateTime.now(), OffsetDateTime.now(), version);
    }

    private static final class FakeLifecycle implements SupplierExportTaskLifecyclePort {
        private final List<String> events = new ArrayList<>();
        private String completedObjectKey;
        private String failureReason;
        private OffsetDateTime nextRetryAt;

        @Override
        public boolean claim(long taskId, int version) {
            events.add("claim");
            return true;
        }

        @Override
        public void complete(long taskId, int processingVersion, SupplierExportObjectStoragePort.StoredObject stored,
                             String fileName, String downloadUrl) {
            events.add("complete");
            completedObjectKey = stored.objectKey();
        }

        @Override
        public void fail(long taskId, int processingVersion, String reason, OffsetDateTime retryAt) {
            events.add("fail");
            failureReason = reason;
            nextRetryAt = retryAt;
        }
    }

    private static final class FakeStorage implements SupplierExportObjectStoragePort {
        private final boolean fail;
        private byte[] lastContent;

        private FakeStorage(boolean fail) {
            this.fail = fail;
        }

        @Override
        public StoredObject store(String objectKey, byte[] content, String contentType) {
            if (fail) {
                throw new IllegalStateException("storage unavailable");
            }
            lastContent = content;
            return new StoredObject(objectKey, contentType, content.length);
        }

        @Override
        public StoredContent load(String objectKey) {
            return new StoredContent(lastContent, "text/csv;charset=UTF-8");
        }
    }
}
