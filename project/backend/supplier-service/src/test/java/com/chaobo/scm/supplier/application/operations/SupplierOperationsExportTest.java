package com.chaobo.scm.supplier.application.operations;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.supplier.application.shared.AuditLogRepository;
import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.operations.SupplierOperationsMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierOperationsExportTest {
    private final FakeOperationsMapper fakeMapper = new FakeOperationsMapper();
    private final SupplierOperationsMapper mapper = fakeMapper.proxy();
    private final SupplierOperationsApplicationService service = new SupplierOperationsApplicationService(
            mapper, new FixedIdentifierGenerator(), noopAudit());

    @Test
    void createExportUsesSupplierScopeAndWritesTask() {
        var id = service.createExport("WARNING", 3001L, "{\"status\":1}", context(3001L,
                "supplier:export:create"));

        assertThat(id).isEqualTo(9001L);
        assertThat(fakeMapper.insertedExportId).isEqualTo(9001L);
        assertThat(fakeMapper.insertedExportType).isEqualTo("WARNING");
        assertThat(fakeMapper.insertedSupplierId).isEqualTo(3001L);
        assertThat(fakeMapper.insertedQueryJson).isEqualTo("{\"status\":1}");
    }

    @Test
    void createExportRejectsUnsupportedType() {
        assertThatThrownBy(() -> service.createExport("UNKNOWN", null, "{}", context(null,
                "supplier:export:create")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的导出类型");
    }

    @Test
    void completeExportRequiresSuccessfulVersionUpdate() {
        fakeMapper.completeResult = 1;

        service.completeExport(9001L, 0, "oss://bucket/file.csv", context(null,
                "supplier:export:complete"));

        assertThat(fakeMapper.completedExportId).isEqualTo(9001L);
        assertThat(fakeMapper.completedFileUrl).isEqualTo("oss://bucket/file.csv");
    }

    private CommandContext context(Long supplierScopeId, String permission) {
        return new CommandContext(1001L, "测试用户", 1L, supplierScopeId, "REQ-1", "TRACE-1",
                "IDEMP-1", Set.of(permission));
    }

    private AuditLogRepository noopAudit() {
        return (context, operationType, targetType, targetId, targetNo, beforeSnapshot, afterSnapshot) -> {
        };
    }

    private static final class FakeOperationsMapper {
        long insertedExportId;
        String insertedExportType;
        Long insertedSupplierId;
        String insertedQueryJson;
        long completedExportId;
        String completedFileUrl;
        int completeResult;

        SupplierOperationsMapper proxy() {
            return (SupplierOperationsMapper) Proxy.newProxyInstance(
                    SupplierOperationsMapper.class.getClassLoader(),
                    new Class<?>[]{SupplierOperationsMapper.class},
                    (target, method, args) -> switch (method.getName()) {
                        case "insertExport" -> {
                            insertedExportId = (Long) args[0];
                            insertedExportType = (String) args[1];
                            insertedSupplierId = (Long) args[2];
                            insertedQueryJson = (String) args[3];
                            yield null;
                        }
                        case "completeExport" -> {
                            completedExportId = (Long) args[0];
                            completedFileUrl = (String) args[2];
                            yield completeResult;
                        }
                        case "workItems", "warnings", "failedInbound", "failedOutbound", "reconciliations",
                             "exportTasks" -> java.util.List.of();
                        case "exportTask", "dashboard" -> null;
                        case "localAsnCount", "localReturnCount", "localStatementCount" -> 0L;
                        case "localStatementAmount" -> BigDecimal.ZERO;
                        case "insertWork", "insertWarning", "processWork", "processWarning", "replayInbound",
                             "replayOutbound", "failExport" -> 1;
                        case "upsertReconciliation" -> null;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }

    private static final class FixedIdentifierGenerator implements IdentifierGenerator {
        @Override
        public long nextId() {
            return 9001L;
        }

        @Override
        public String nextBusinessNo(String prefix) {
            return prefix + "9001";
        }
    }
}
