package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.application.storage.BmsReportObjectStoragePort;
import com.chaobo.scm.bms.infrastructure.persistence.BmsReportExportMapper;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BMS 异步报表导出生命周期测试。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class BmsReportExportApplicationServiceTest {

    @Test
    void exportsTraceableCsvWithExactDecimalAmountsAndAuthorizedDownload()
        throws Exception {
        MemoryExportMapper tasks = new MemoryExportMapper();
        RecordingStorage storage = new RecordingStorage();
        BmsReportExportApplicationService service =
            new BmsReportExportApplicationService(tasks,
                new BmsReadQueryApplicationServiceTest.QueryMapper(), storage, 3);
        ScmAccessContext access = access("BO-A");

        BmsReportExportMapper.ExportTaskRow created = service.enqueue(
            new BmsReportExportApplicationService.CreateCommand(
                "BO-A", "2026-07", "export-1"), access);

        assertThat(service.dispatch(10)).isEqualTo(1);
        BmsReportExportMapper.ExportTaskRow completed = tasks.find(created.exportNo());
        assertThat(completed.status()).isEqualTo(4);
        assertThat(completed.recordCount()).isEqualTo(1);
        assertThat(storage.content).contains("100.10,80.08,10.01,90.09");
        assertThat(service.download(created.exportNo(), access).inputStream().readAllBytes())
            .isEqualTo(storage.content.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> service.download(created.exportNo(), access("BO-B")))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void finalFailureCanBeRecoveredButOnlyInsideBillingObjectScope() {
        MemoryExportMapper tasks = new MemoryExportMapper();
        RecordingStorage storage = new RecordingStorage();
        storage.fail = true;
        BmsReportExportApplicationService service =
            new BmsReportExportApplicationService(tasks,
                new BmsReadQueryApplicationServiceTest.QueryMapper(), storage, 1);
        BmsReportExportMapper.ExportTaskRow task = service.enqueue(
            new BmsReportExportApplicationService.CreateCommand(
                "BO-A", "2026-07", "export-fail"), access("BO-A"));

        assertThat(service.dispatch(1)).isZero();
        assertThat(tasks.find(task.exportNo()).status()).isEqualTo(5);
        assertThatThrownBy(() -> service.retry(task.exportNo(), access("BO-B")))
            .isInstanceOf(BusinessException.class);
        service.retry(task.exportNo(), access("BO-A"));
        assertThat(tasks.find(task.exportNo()).status()).isEqualTo(1);
    }

    private static ScmAccessContext access(String objectCode) {
        return new ScmAccessContext(1001, "finance", "BMS",
            Set.of("bms:report:export", "bms:report:download"),
            Map.of("BILLING_OBJECT", Set.of(objectCode)));
    }

    private static final class RecordingStorage
        implements BmsReportObjectStoragePort {

        private String content;
        private boolean fail;

        @Override
        public String put(String objectKey, byte[] value, String contentType)
            throws java.io.IOException {
            if (fail) {
                throw new java.io.IOException("storage unavailable");
            }
            content = new String(value, StandardCharsets.UTF_8);
            return "bms-object://" + objectKey;
        }

        @Override
        public StoredObject open(String objectReference) {
            byte[] value = content.getBytes(StandardCharsets.UTF_8);
            return new StoredObject(new ByteArrayInputStream(value), value.length,
                "text/csv", "report.csv");
        }
    }

    private static final class MemoryExportMapper implements BmsReportExportMapper {

        private final Map<String, ExportTaskRow> rows = new LinkedHashMap<>();

        @Override
        public ExportTaskRow findByIdempotencyKey(String idempotencyKey) {
            return rows.values().stream()
                .filter(row -> row.idempotencyKey().equals(idempotencyKey))
                .findFirst().orElse(null);
        }

        @Override
        public ExportTaskRow find(String exportNo) {
            return rows.get(exportNo);
        }

        @Override
        public List<ExportTaskRow> list(String objectCode) {
            return rows.values().stream()
                .filter(row -> objectCode == null || row.objectCode().equals(objectCode))
                .toList();
        }

        @Override
        public int insert(ExportTaskRow row) {
            rows.put(row.exportNo(), row);
            return 1;
        }

        @Override
        public List<ExportTaskRow> claimable(int limit) {
            return rows.values().stream()
                .filter(row -> row.status() == 1 || row.status() == 3)
                .limit(limit).toList();
        }

        @Override
        public int claim(String exportNo, long version) {
            ExportTaskRow row = rows.get(exportNo);
            if (row == null || row.version() != version
                || row.status() != 1 && row.status() != 3) {
                return 0;
            }
            rows.put(exportNo, copy(row, 2, row.attemptCount() + 1,
                row.objectReference(), row.recordCount(), row.lastError(),
                row.version() + 1));
            return 1;
        }

        @Override
        public int markSucceeded(String exportNo, String objectReference,
                                 long recordCount) {
            ExportTaskRow row = rows.get(exportNo);
            rows.put(exportNo, copy(row, 4, row.attemptCount(),
                objectReference, recordCount, null, row.version() + 1));
            return 1;
        }

        @Override
        public int markFailed(String exportNo, int status, String lastError) {
            ExportTaskRow row = rows.get(exportNo);
            rows.put(exportNo, copy(row, status, row.attemptCount(),
                row.objectReference(), row.recordCount(), lastError,
                row.version() + 1));
            return 1;
        }

        @Override
        public int retryFinalFailure(String exportNo) {
            ExportTaskRow row = rows.get(exportNo);
            if (row == null || row.status() != 5) {
                return 0;
            }
            rows.put(exportNo, copy(row, 1, 0, row.objectReference(),
                row.recordCount(), null, row.version() + 1));
            return 1;
        }

        private ExportTaskRow copy(ExportTaskRow row, int status, int attempts,
                                   String reference, long count, String error,
                                   long version) {
            return new ExportTaskRow(row.exportNo(), row.objectCode(),
                row.billingPeriod(), row.idempotencyKey(), status, attempts,
                row.maxAttempts(), null, reference, count, error, row.operatorId(),
                version, LocalDateTime.now(), LocalDateTime.now());
        }
    }
}
