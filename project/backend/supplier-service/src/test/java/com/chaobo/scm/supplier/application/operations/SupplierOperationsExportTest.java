package com.chaobo.scm.supplier.application.operations;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.application.operations.export.SupplierExportObjectStoragePort;
import com.chaobo.scm.supplier.application.shared.AuditLogRepository;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.operations.SupplierOperationsMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证导出任务的数据范围、持久化幂等和人工重试规则。
 */
class SupplierOperationsExportTest {

    private static final long GENERATED_TASK_ID = 9001L;
    private final FakeOperationsMapper fake = new FakeOperationsMapper();
    private final SupplierOperationsApplicationService service = new SupplierOperationsApplicationService(
            fake.proxy(), new FixedIdentifierGenerator(), noopAudit(), new FakeStorage());

    @Test
    void createExportUsesSupplierScopeAndReturnsPersistedTask() {
        long id = service.createExport("WARNING", 3001L, "{\"status\":1}",
                context(3001L, "supplier:export:create", "IDEMP-1"));

        assertThat(id).isEqualTo(GENERATED_TASK_ID);
        assertThat(fake.insertedSupplierId).isEqualTo(3001L);
        assertThat(fake.insertedIdempotencyKey).isEqualTo("IDEMP-1");
    }

    @Test
    void repeatedSameIdempotencyKeyReturnsOriginalTask() {
        fake.persistedId = 8001L;

        long id = service.createExport("WARNING", 3001L, "{}",
                context(3001L, "supplier:export:create", "IDEMP-1"));

        assertThat(id).isEqualTo(8001L);
    }

    @Test
    void sameIdempotencyKeyWithDifferentPayloadIsRejected() {
        fake.persistedId = 8002L;
        fake.persistedQuery = "{\"status\":2}";

        assertThatThrownBy(() -> service.createExport("WARNING", 3001L, "{\"status\":1}",
                context(3001L, "supplier:export:create", "IDEMP-1")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("幂等键");
    }

    @Test
    void supplierAccountCannotExportGlobalFailureEvents() {
        assertThatThrownBy(() -> service.createExport("FAILED_EVENT", 3001L, "{}",
                context(3001L, "supplier:export:create", "IDEMP-1")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("全局运营数据");
    }

    @Test
    void retryRequiresFailedTaskVersionAndSupplierScope() {
        fake.retryResult = 1;

        service.retryExport(GENERATED_TASK_ID, 4, context(3001L, "supplier:export:retry", "RETRY-1"));

        assertThat(fake.retriedId).isEqualTo(GENERATED_TASK_ID);
        assertThat(fake.retriedVersion).isEqualTo(4);
        assertThat(fake.retryScope).isEqualTo(3001L);
    }

    private CommandContext context(Long supplierScopeId, String permission, String idempotencyKey) {
        return new CommandContext(1001L, "测试用户", 1L, supplierScopeId, "REQ-1", "TRACE-1",
                idempotencyKey, Set.of(permission));
    }

    private AuditLogRepository noopAudit() {
        return (context, operationType, targetType, targetId, targetNo, beforeSnapshot, afterSnapshot) -> {
        };
    }

    private static OperationViews.ExportTask task(long id, String query) {
        return new OperationViews.ExportTask(id, "WARNING", 3001L, query, 1, null, null,
                null, null, null, null, 0, null, null, null,
                OffsetDateTime.now(), OffsetDateTime.now(), 0);
    }

    private static final class FakeOperationsMapper {
        private long persistedId = GENERATED_TASK_ID;
        private String persistedQuery = "{}";
        private Long insertedSupplierId;
        private String insertedIdempotencyKey;
        private int retryResult;
        private long retriedId;
        private int retriedVersion;
        private Long retryScope;

        SupplierOperationsMapper proxy() {
            return (SupplierOperationsMapper) Proxy.newProxyInstance(SupplierOperationsMapper.class.getClassLoader(),
                    new Class<?>[] {SupplierOperationsMapper.class}, (target, method, args) -> switch (method.getName()) {
                        case "insertExport" -> {
                            insertedSupplierId = (Long) args[2];
                            insertedIdempotencyKey = (String) args[5];
                            if (persistedId == GENERATED_TASK_ID) {
                                persistedQuery = (String) args[3];
                            }
                            yield 1;
                        }
                        case "exportTaskByIdempotency" -> task(persistedId, persistedQuery);
                        case "retryExport" -> {
                            retriedId = (Long) args[0];
                            retriedVersion = (Integer) args[1];
                            retryScope = (Long) args[2];
                            yield retryResult;
                        }
                        case "workItems", "warnings", "failedInbound", "failedOutbound", "reconciliations",
                             "exportTasks", "claimableExports" -> List.of();
                        case "exportTask", "dashboard" -> null;
                        case "localAsnCount", "localReturnCount", "localStatementCount" -> 0L;
                        case "localStatementAmount" -> java.math.BigDecimal.ZERO;
                        case "insertWork", "insertWarning", "processWork", "processWarning", "replayInbound",
                             "replayOutbound", "claimExport", "completeExport", "failExport" -> 1;
                        case "upsertReconciliation" -> null;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }

    private static final class FixedIdentifierGenerator implements IdentifierGenerator {
        @Override
        public long nextId() {
            return GENERATED_TASK_ID;
        }

        @Override
        public String nextBusinessNo(String prefix) {
            return prefix + "-9001";
        }
    }

    private static final class FakeStorage implements SupplierExportObjectStoragePort {
        @Override
        public StoredObject store(String objectKey, byte[] content, String contentType) {
            return new StoredObject(objectKey, contentType, content.length);
        }

        @Override
        public StoredContent load(String objectKey) {
            return new StoredContent(new byte[0], "text/csv;charset=UTF-8");
        }
    }
}
