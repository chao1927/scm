package com.chaobo.scm.oms.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.oms.infrastructure.persistence.OmsFulfillmentMetricsMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** OMS 履约指标口径、数据范围和导出权限测试。 */
class OmsFulfillmentMetricsApplicationServiceTest {

    @Test
    void calculatesTraceableCohortMetricsWithinAllThreeScopes() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        List<OmsFulfillmentMetricsMapper.MetricFactRow> facts = List.of(
                fact("SO-1", 10, 20, start, "FUL-1A", 30, 5, start.plusHours(2), false),
                fact("SO-1", 10, 20, start, "FUL-1B", 30, 5, start.plusHours(3), false),
                fact("SO-2", 10, 20, start.plusHours(1), "FUL-2", 30, 6,
                        start.plusHours(2), true),
                fact("SO-3", 10, 20, start.plusHours(2), "FUL-3", 30, 1,
                        start.plusHours(2), false),
                fact("SO-DENIED", 11, 20, start, "FUL-4", 30, 5,
                        start.plusHours(1), false));
        var service = service(mapper((method, arguments) ->
                "listMetricFacts".equals(method) ? facts : null), new MemoryStorage());

        var result = service.metrics(start, start.plusDays(1),
                access("oms:metrics:read", Set.of("10"), Set.of("20"), Set.of("30")));

        assertThat(result.orderCount()).isEqualTo(3);
        assertThat(result.completedOrderCount()).isEqualTo(1);
        assertThat(result.cancelledOrderCount()).isEqualTo(1);
        assertThat(result.fulfillmentRate()).isEqualByComparingTo("0.3333");
        assertThat(result.averageFulfillmentDurationSeconds()).isEqualTo(10800);
        assertThat(result.sourceTables()).containsExactly(
                "oms_sales_order", "oms_fulfillment", "oms_cancel_request");
    }

    @Test
    void createExportIsIdempotentAndRejectsChangedRequest() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        AtomicReference<OmsFulfillmentMetricsMapper.ExportTaskRow> persisted =
                new AtomicReference<>();
        var mapper = mapper((method, arguments) -> {
            if ("insertExport".equals(method)) {
                var row = (OmsFulfillmentMetricsMapper.ExportTaskRow) arguments[0];
                if (persisted.get() == null) {
                    persisted.set(withPersistenceFields(row));
                    return 1;
                }
                return 0;
            }
            if ("findByIdempotency".equals(method)) {
                return persisted.get();
            }
            return null;
        });
        var service = service(mapper, new MemoryStorage());
        var access = access("oms:metrics:export",
                Set.of("10"), Set.of("20"), Set.of("30"));

        var first = service.createExport(start, start.plusDays(1), "idem-1", access);
        var duplicate = service.createExport(start, start.plusDays(1), "idem-1", access);

        assertThat(duplicate.exportNo()).isEqualTo(first.exportNo());
        assertThatThrownBy(() -> service.createExport(
                start, start.plusDays(2), "idem-1", access))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code())
                                .isEqualTo(ErrorCode.IDEMPOTENCY_CONFLICT));
    }

    @Test
    void downloadRechecksCapturedScopeAfterPermissionRevocation() {
        LocalDateTime now = LocalDateTime.now();
        var task = new OmsFulfillmentMetricsMapper.ExportTaskRow(
                1L, "OMS-MET-1", now.minusDays(1), now,
                "*", "20", "30", 1L, "idem", "hash", 4, 1,
                null, null, "key/file.csv", "file.csv", "text/csv", 10L,
                1, null, 2, now, now, now);
        var service = service(mapper((method, arguments) ->
                "findExport".equals(method) ? task : null), new MemoryStorage());

        assertThatThrownBy(() -> service.download("OMS-MET-1",
                access("oms:metrics:download", Set.of("10"),
                        Set.of("20"), Set.of("30"))))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.code())
                                .isEqualTo(ErrorCode.NOT_FOUND));
    }

    private static OmsFulfillmentMetricsApplicationService service(
            OmsFulfillmentMetricsMapper mapper, OmsMetricExportObjectStoragePort storage) {
        return new OmsFulfillmentMetricsApplicationService(mapper, storage);
    }

    private static OmsFulfillmentMetricsMapper.MetricFactRow fact(
            String orderNo, long organizationId, long ownerId,
            LocalDateTime orderCreatedAt, String fulfillmentNo, long warehouseId,
            int status, LocalDateTime updatedAt, boolean cancelled) {
        return new OmsFulfillmentMetricsMapper.MetricFactRow(
                orderNo, organizationId, ownerId, orderCreatedAt,
                fulfillmentNo, warehouseId, status, updatedAt, cancelled);
    }

    private static OmsFulfillmentMetricsMapper.ExportTaskRow withPersistenceFields(
            OmsFulfillmentMetricsMapper.ExportTaskRow row) {
        LocalDateTime now = LocalDateTime.now();
        return new OmsFulfillmentMetricsMapper.ExportTaskRow(
                1L, row.exportNo(), row.periodStart(), row.periodEnd(),
                row.organizationScope(), row.ownerScope(), row.warehouseScope(),
                row.requestedBy(), row.idempotencyKey(), row.requestHash(),
                row.status(), row.attemptCount(), null, null, null, null,
                null, null, null, null, row.version(), now, now, null);
    }

    private static ScmAccessContext access(
            String permission, Set<String> organizations,
            Set<String> owners, Set<String> warehouses) {
        return new ScmAccessContext(1, "tester", "OMS", Set.of(permission),
                Map.of("ORGANIZATION", organizations, "OWNER", owners,
                        "WAREHOUSE", warehouses));
    }

    private static OmsFulfillmentMetricsMapper mapper(Handler handler) {
        return (OmsFulfillmentMetricsMapper) Proxy.newProxyInstance(
                OmsFulfillmentMetricsMapper.class.getClassLoader(),
                new Class<?>[]{OmsFulfillmentMetricsMapper.class},
                (proxy, method, arguments) -> handler.invoke(method.getName(), arguments));
    }

    @FunctionalInterface
    private interface Handler {
        Object invoke(String method, Object[] arguments);
    }

    private static final class MemoryStorage implements OmsMetricExportObjectStoragePort {
        private final List<byte[]> contents = new ArrayList<>();

        @Override
        public StoredObject store(String objectKey, byte[] content, String contentType) {
            contents.add(content);
            return new StoredObject(objectKey, contentType, content.length);
        }

        @Override
        public StoredContent load(String objectKey) {
            byte[] bytes = contents.isEmpty() ? new byte[]{1} : contents.get(0);
            return new StoredContent(bytes, "text/csv;charset=UTF-8");
        }
    }
}
