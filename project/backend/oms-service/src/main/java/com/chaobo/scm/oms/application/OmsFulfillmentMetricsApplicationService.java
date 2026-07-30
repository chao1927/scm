package com.chaobo.scm.oms.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.oms.infrastructure.persistence.OmsFulfillmentMetricsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OMS 履约指标查询与导出任务应用服务。
 *
 * <p>指标以订单创建时间窗为 cohort，仅读取 OMS 自有订单、履约和取消事实。
 * 导出任务捕获创建时的数据范围，下载时再次校验当前权限。
 */
@Service
public class OmsFulfillmentMetricsApplicationService {

    private static final int FULFILLMENT_SHIPPED = 5;
    private static final int FULFILLMENT_CANCELLED = 6;
    private static final int EXPORT_COMPLETED = 4;
    private static final int EXPORT_RETRY_WAIT = 3;
    private static final int EXPORT_FINAL_FAILED = 5;
    private static final int MAX_PERIOD_DAYS = 366;
    private static final int EXPORT_LIST_LIMIT = 100;

    private final OmsFulfillmentMetricsMapper mapper;
    private final OmsMetricExportObjectStoragePort storage;

    public OmsFulfillmentMetricsApplicationService(
            OmsFulfillmentMetricsMapper mapper,
            OmsMetricExportObjectStoragePort storage) {
        this.mapper = mapper;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public MetricsView metrics(
            LocalDateTime periodStart, LocalDateTime periodEnd,
            ScmAccessContext access) {
        access.requirePermission("oms:metrics:read");
        Period period = period(periodStart, periodEnd);
        ScopeSnapshot scope = ScopeSnapshot.from(access);
        return calculate(period, scope).summary();
    }

    @Transactional
    public ExportTaskView createExport(
            LocalDateTime periodStart, LocalDateTime periodEnd,
            String idempotencyKey, ScmAccessContext access) {
        access.requirePermission("oms:metrics:export");
        Period period = period(periodStart, periodEnd);
        ScopeSnapshot scope = ScopeSnapshot.from(access);
        String key = required(idempotencyKey, "幂等键", 128);
        String requestHash = hash(period.start() + "|" + period.end() + "|"
                + scope.organizations() + "|" + scope.owners() + "|"
                + scope.warehouses());
        String exportNo = "OMS-MET-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 20).toUpperCase();
        mapper.insertExport(new OmsFulfillmentMetricsMapper.ExportTaskRow(
                null, exportNo, period.start(), period.end(),
                scope.organizations(), scope.owners(), scope.warehouses(),
                access.operatorId(), key, requestHash, 1, 0,
                null, null, null, null, null, null, null, null,
                1, null, null, null));
        var persisted = mapper.findByIdempotency(access.operatorId(), key);
        if (persisted == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出任务创建结果不可用");
        }
        if (!requestHash.equals(persisted.requestHash())) {
            throw new BusinessException(
                    ErrorCode.IDEMPOTENCY_CONFLICT, "幂等键已被不同导出请求使用");
        }
        return exportView(persisted);
    }

    @Transactional(readOnly = true)
    public List<ExportTaskView> exports(ScmAccessContext access) {
        access.requirePermission("oms:metrics:read");
        ScopeSnapshot current = ScopeSnapshot.from(access);
        return mapper.listExports(access.operatorId(), EXPORT_LIST_LIMIT).stream()
                .filter(task -> current.covers(new ScopeSnapshot(
                        task.organizationScope(), task.ownerScope(), task.warehouseScope())))
                .map(this::exportView).toList();
    }

    @Transactional(readOnly = true)
    public ExportTaskView export(String exportNo, ScmAccessContext access) {
        access.requirePermission("oms:metrics:read");
        return exportView(requireOwnedExport(exportNo, access));
    }

    @Transactional
    public ExportTaskView retry(
            String exportNo, long version, ScmAccessContext access) {
        access.requirePermission("oms:metrics:retry");
        var task = requireOwnedExport(exportNo, access);
        if (task.status() != EXPORT_RETRY_WAIT
                && task.status() != EXPORT_FINAL_FAILED) {
            throw validation("仅等待重试或最终失败任务可人工重试");
        }
        if (mapper.retryExport(task.exportNo(), access.operatorId(), version) != 1) {
            throw new BusinessException(
                    ErrorCode.VERSION_CONFLICT, "导出任务状态或版本已变更");
        }
        return exportView(mapper.findExport(task.exportNo()));
    }

    @Transactional(readOnly = true)
    public ExportFile download(String exportNo, ScmAccessContext access) {
        access.requirePermission("oms:metrics:download");
        var task = requireOwnedExport(exportNo, access);
        ScopeSnapshot captured = new ScopeSnapshot(
                task.organizationScope(), task.ownerScope(), task.warehouseScope());
        if (!ScopeSnapshot.from(access).covers(captured)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在或无权访问");
        }
        if (task.status() != EXPORT_COMPLETED
                || task.objectKey() == null || task.objectKey().isBlank()) {
            throw validation("导出文件尚未生成");
        }
        var content = storage.load(task.objectKey());
        return new ExportFile(task.fileName(), content.contentType(), content.bytes());
    }

    MetricResult calculate(Period period, ScopeSnapshot scope) {
        List<OmsFulfillmentMetricsMapper.MetricFactRow> source =
                mapper.listMetricFacts(period.start(), period.end());
        Map<String, List<OmsFulfillmentMetricsMapper.MetricFactRow>> grouped = source.stream()
                .filter(scope::allowsOrganizationAndOwner)
                .collect(Collectors.groupingBy(
                        OmsFulfillmentMetricsMapper.MetricFactRow::orderNo,
                        LinkedHashMap::new, Collectors.toList()));
        List<OrderMetricRow> rows = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<OmsFulfillmentMetricsMapper.MetricFactRow> scopedRows =
                    scope.fulfillments(entry.getValue());
            if (scopedRows.isEmpty()) {
                continue;
            }
            rows.add(orderMetric(scopedRows));
        }
        rows.sort(Comparator.comparing(OrderMetricRow::orderNo));
        long completed = rows.stream().filter(row -> "COMPLETED".equals(row.outcome())).count();
        long cancelled = rows.stream().filter(row -> "CANCELLED".equals(row.outcome())).count();
        List<Long> durations = rows.stream().map(OrderMetricRow::fulfillmentDurationSeconds)
                .filter(value -> value != null).toList();
        BigDecimal rate = rows.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed)
                .divide(BigDecimal.valueOf(rows.size()), 4, RoundingMode.HALF_UP);
        Long averageSeconds = durations.isEmpty() ? null
                : Math.round(durations.stream().mapToLong(Long::longValue).average().orElse(0));
        MetricsView summary = new MetricsView(
                period.start(), period.end(), rows.size(), completed, cancelled,
                rate, averageSeconds,
                "订单创建时间落在 [periodStart, periodEnd) 的去重销售订单",
                "存在履约，至少一个已发货，且其他履约均已发货或已取消",
                "完成取消申请，或订单所有履约均已取消",
                "完成订单数 / 订单数",
                "订单创建时间至最后一个有效履约发货时间",
                List.of("oms_sales_order", "oms_fulfillment", "oms_cancel_request"),
                LocalDateTime.now());
        return new MetricResult(summary, rows);
    }

    private OrderMetricRow orderMetric(
            List<OmsFulfillmentMetricsMapper.MetricFactRow> rows) {
        var first = rows.get(0);
        List<OmsFulfillmentMetricsMapper.MetricFactRow> fulfillments = rows.stream()
                .filter(row -> row.fulfillmentNo() != null).toList();
        boolean cancellationCompleted = rows.stream()
                .anyMatch(OmsFulfillmentMetricsMapper.MetricFactRow::cancellationCompleted);
        boolean allCancelled = !fulfillments.isEmpty() && fulfillments.stream()
                .allMatch(row -> row.fulfillmentStatus() == FULFILLMENT_CANCELLED);
        boolean hasShipped = fulfillments.stream()
                .anyMatch(row -> row.fulfillmentStatus() == FULFILLMENT_SHIPPED);
        boolean terminal = !fulfillments.isEmpty() && fulfillments.stream()
                .allMatch(row -> row.fulfillmentStatus() == FULFILLMENT_SHIPPED
                        || row.fulfillmentStatus() == FULFILLMENT_CANCELLED);
        String outcome;
        if (cancellationCompleted || allCancelled) {
            outcome = "CANCELLED";
        } else if (hasShipped && terminal) {
            outcome = "COMPLETED";
        } else {
            outcome = "IN_PROGRESS";
        }
        LocalDateTime completedAt = "COMPLETED".equals(outcome)
                ? fulfillments.stream()
                .filter(row -> row.fulfillmentStatus() == FULFILLMENT_SHIPPED)
                .map(OmsFulfillmentMetricsMapper.MetricFactRow::fulfillmentUpdatedAt)
                .max(LocalDateTime::compareTo).orElse(null) : null;
        Long durationSeconds = completedAt == null
                || completedAt.isBefore(first.orderCreatedAt()) ? null
                : Duration.between(first.orderCreatedAt(), completedAt).getSeconds();
        String warehouseIds = fulfillments.stream()
                .map(OmsFulfillmentMetricsMapper.MetricFactRow::warehouseId)
                .filter(value -> value != null).distinct().sorted()
                .map(String::valueOf).collect(Collectors.joining(","));
        return new OrderMetricRow(first.orderNo(), first.organizationId(), first.ownerId(),
                warehouseIds, first.orderCreatedAt(), outcome, completedAt,
                durationSeconds, fulfillments.size());
    }

    private OmsFulfillmentMetricsMapper.ExportTaskRow requireOwnedExport(
            String exportNo, ScmAccessContext access) {
        var task = mapper.findExport(required(exportNo, "导出任务号", 64));
        boolean visible = task != null && task.requestedBy() == access.operatorId()
                && ScopeSnapshot.from(access).covers(new ScopeSnapshot(
                task.organizationScope(), task.ownerScope(), task.warehouseScope()));
        if (!visible) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在或无权访问");
        }
        return task;
    }

    private ExportTaskView exportView(OmsFulfillmentMetricsMapper.ExportTaskRow row) {
        return new ExportTaskView(row.exportNo(), row.periodStart(), row.periodEnd(),
                row.status(), exportStatus(row.status()), row.attemptCount(),
                row.recordCount(), row.fileName(), row.fileSize(), row.lastError(),
                row.version(), row.createdAt(), row.updatedAt(), row.completedAt());
    }

    private static Period period(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw validation("指标时间窗必须满足 periodStart < periodEnd");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_PERIOD_DAYS) {
            throw validation("指标时间窗不能超过 366 天");
        }
        return new Period(start, end);
    }

    private static String required(String value, String label, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw validation(label + "格式错误");
        }
        return value.trim();
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK 缺少 SHA-256", exception);
        }
    }

    private static String exportStatus(int status) {
        return switch (status) {
            case 1 -> "待生成";
            case 2 -> "生成中";
            case 3 -> "等待重试";
            case 4 -> "已完成";
            case 5 -> "最终失败";
            default -> "未知";
        };
    }

    private static BusinessException validation(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    record Period(LocalDateTime start, LocalDateTime end) {
    }

    record ScopeSnapshot(String organizations, String owners, String warehouses) {
        static ScopeSnapshot from(ScmAccessContext access) {
            return new ScopeSnapshot(serialize(access, "ORGANIZATION"),
                    serialize(access, "OWNER"), serialize(access, "WAREHOUSE"));
        }

        boolean allowsOrganizationAndOwner(
                OmsFulfillmentMetricsMapper.MetricFactRow row) {
            return allows(organizations, row.organizationId())
                    && allows(owners, row.ownerId());
        }

        List<OmsFulfillmentMetricsMapper.MetricFactRow> fulfillments(
                List<OmsFulfillmentMetricsMapper.MetricFactRow> rows) {
            if ("*".equals(warehouses)) {
                return rows;
            }
            if (warehouses.isBlank()) {
                return List.of();
            }
            return rows.stream().filter(row -> allows(warehouses, row.warehouseId())).toList();
        }

        boolean covers(ScopeSnapshot captured) {
            return covers(organizations, captured.organizations)
                    && covers(owners, captured.owners)
                    && covers(warehouses, captured.warehouses);
        }

        private static String serialize(ScmAccessContext access, String type) {
            Set<String> values = access.dataScopes().getOrDefault(type, Set.of());
            if (values.contains("*")) {
                return "*";
            }
            try {
                return values.stream().map(Long::valueOf).filter(value -> value > 0)
                        .sorted().map(String::valueOf).collect(Collectors.joining(","));
            } catch (NumberFormatException exception) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "数据范围包含非法标识");
            }
        }

        private static boolean allows(String serialized, Long value) {
            return "*".equals(serialized) || value != null
                    && List.of(serialized.split(",")).contains(String.valueOf(value));
        }

        private static boolean covers(String current, String captured) {
            if ("*".equals(current)) {
                return true;
            }
            if ("*".equals(captured)) {
                return false;
            }
            Set<String> currentValues = Set.of(current.split(","));
            return currentValues.containsAll(Set.of(captured.split(",")));
        }
    }

    record MetricResult(MetricsView summary, List<OrderMetricRow> rows) {
    }

    public record MetricsView(LocalDateTime periodStart, LocalDateTime periodEnd,
                              long orderCount, long completedOrderCount,
                              long cancelledOrderCount, BigDecimal fulfillmentRate,
                              Long averageFulfillmentDurationSeconds,
                              String orderCountDefinition,
                              String completedDefinition,
                              String cancelledDefinition,
                              String fulfillmentRateDefinition,
                              String durationDefinition, List<String> sourceTables,
                              LocalDateTime generatedAt) {
    }

    record OrderMetricRow(String orderNo, Long organizationId, Long ownerId,
                          String warehouseIds, LocalDateTime orderCreatedAt,
                          String outcome, LocalDateTime completedAt,
                          Long fulfillmentDurationSeconds, int fulfillmentCount) {
    }

    public record ExportTaskView(String exportNo, LocalDateTime periodStart,
                                 LocalDateTime periodEnd, int status,
                                 String statusName, int attemptCount,
                                 Integer recordCount, String fileName,
                                 Long fileSize, String lastError, long version,
                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                 LocalDateTime completedAt) {
    }

    public record ExportFile(String fileName, String contentType, byte[] bytes) {
    }
}
