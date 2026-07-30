package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 库存运营查询应用服务。
 *
 * <p>应用层同时校验功能权限与仓库/货主范围，再把不可扩大的范围交给只读端口。
 * 没有任一数据范围时安全返回空分页，绝不把缺失范围解释为查看全部。
 *
 * @author SCM Team
 */
@Service
public class InventoryOperationsQueryApplicationService {

    private static final String OWNER_SCOPE = "OWNER";
    private static final String WAREHOUSE_SCOPE = "WAREHOUSE";
    private static final String WILDCARD = "*";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_SLOW_MOVING_DAYS = 90;
    private static final int DEFAULT_EXPIRY_WARNING_DAYS = 30;

    private final InventoryOperationReadModelPort readModel;

    public InventoryOperationsQueryApplicationService(
            InventoryOperationReadModelPort readModel) {
        this.readModel = readModel;
    }

    public PageResult<InventoryOperationReadModelPort.ReservationView> reservations(
            OperationQuery query,
            ScmAccessContext access) {
        access.requirePermission("inventory:reservation:read");
        return page(readModelPage(
                scope(access, query),
                query,
                readModel::reservations), query);
    }

    public PageResult<InventoryOperationReadModelPort.FreezeView> freezes(
            OperationQuery query,
            ScmAccessContext access) {
        access.requirePermission("inventory:freeze:read");
        return page(readModelPage(
                scope(access, query),
                query,
                readModel::freezes), query);
    }

    public PageResult<InventoryOperationReadModelPort.AdjustmentView> adjustments(
            OperationQuery query,
            ScmAccessContext access) {
        access.requirePermission("inventory:adjustment:read");
        return page(readModelPage(
                scope(access, query),
                query,
                readModel::adjustments), query);
    }

    public PageResult<InventoryOperationReadModelPort.EventLogView> eventLogs(
            OperationQuery query,
            ScmAccessContext access) {
        access.requirePermission("inventory:event_log:read");
        return page(readModelPage(
                scope(access, query),
                query,
                readModel::eventLogs), query);
    }

    public PageResult<InventoryOperationReadModelPort.OperationLogView> operationLogs(
            OperationQuery query,
            ScmAccessContext access) {
        access.requirePermission("inventory:operation_log:read");
        return page(readModelPage(
                scope(access, query),
                query,
                readModel::operationLogs), query);
    }

    public PageResult<InventoryOperationReadModelPort.MetricView> metrics(
            InventoryOperationReadModelPort.MetricType metricType,
            MetricQuery query,
            ScmAccessContext access) {
        access.requirePermission("inventory:metric:read");
        OperationQuery scopeQuery = new OperationQuery(
                query.ownerId(), query.warehouseId(), query.sku(), query.batchNo(),
                null, query.pageNo(), query.pageSize());
        InventoryOperationReadModelPort.QueryScope scope = scope(access, scopeQuery);
        int pageNo = pageNo(query.pageNo());
        int pageSize = pageSize(query.pageSize());
        if (scope.empty()) {
            return new PageResult<>(pageNo, pageSize, 0L, java.util.List.of());
        }
        InventoryOperationReadModelPort.Page<InventoryOperationReadModelPort.MetricView> page =
                readModel.metrics(
                        metricType,
                        scope,
                        new InventoryOperationReadModelPort.MetricFilter(
                                query.sku(),
                                query.batchNo(),
                                positiveOrDefault(
                                        query.slowMovingDays(),
                                        DEFAULT_SLOW_MOVING_DAYS),
                                positiveOrDefault(
                                        query.expiryWarningDays(),
                                        DEFAULT_EXPIRY_WARNING_DAYS),
                                (pageNo - 1) * pageSize,
                                pageSize));
        return new PageResult<>(pageNo, pageSize, page.total(), page.records());
    }

    private <T> InventoryOperationReadModelPort.Page<T> readModelPage(
            InventoryOperationReadModelPort.QueryScope scope,
            OperationQuery query,
            PageLoader<T> loader) {
        if (scope.empty()) {
            return new InventoryOperationReadModelPort.Page<>(0L, java.util.List.of());
        }
        int pageNo = pageNo(query.pageNo());
        int pageSize = pageSize(query.pageSize());
        return loader.load(
                scope,
                new InventoryOperationReadModelPort.OperationFilter(
                        blankToNull(query.sku()),
                        blankToNull(query.batchNo()),
                        query.status(),
                        (pageNo - 1) * pageSize,
                        pageSize,
                        orderBy(query.sortBy(), query.sortDirection())));
    }

    private static InventoryOperationReadModelPort.QueryScope scope(
            ScmAccessContext access,
            OperationQuery query) {
        ScopeValues owners = values(access.dataScopes(), OWNER_SCOPE);
        ScopeValues warehouses = values(access.dataScopes(), WAREHOUSE_SCOPE);
        Set<Long> ownerIds = restrict(owners, query.ownerId(), "货主");
        Set<Long> warehouseIds = restrict(warehouses, query.warehouseId(), "仓库");
        return new InventoryOperationReadModelPort.QueryScope(
                owners.wildcard(), ownerIds, warehouses.wildcard(), warehouseIds);
    }

    private static ScopeValues values(
            Map<String, Set<String>> scopes,
            String scopeType) {
        Set<String> raw = scopes.getOrDefault(scopeType, Set.of());
        boolean wildcard = raw.contains(WILDCARD);
        Set<Long> values = new LinkedHashSet<>();
        for (String value : raw) {
            if (!WILDCARD.equals(value)) {
                try {
                    long parsed = Long.parseLong(value);
                    if (parsed > 0) {
                        values.add(parsed);
                    }
                } catch (NumberFormatException ignored) {
                    // 非法授权值不能扩大范围，安全忽略并最终返回空数据。
                }
            }
        }
        return new ScopeValues(wildcard, Set.copyOf(values));
    }

    private static Set<Long> restrict(
            ScopeValues scope,
            Long requested,
            String name) {
        if (requested == null) {
            return scope.values();
        }
        if (requested <= 0
                || !scope.wildcard() && !scope.values().contains(requested)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    name + "不在授权范围");
        }
        return Set.of(requested);
    }

    private static String orderBy(String sortBy, String direction) {
        String column = switch (sortBy == null ? "updatedAt" : sortBy) {
            case "status" -> "status";
            case "quantity" -> "quantity";
            case "updatedAt" -> "updated_at";
            default -> throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "不支持的排序字段");
        };
        String order = "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
        return column + " " + order;
    }

    private static <T> PageResult<T> page(
            InventoryOperationReadModelPort.Page<T> result,
            OperationQuery query) {
        return new PageResult<>(
                pageNo(query.pageNo()),
                pageSize(query.pageSize()),
                result.total(),
                result.records());
    }

    private static int pageNo(int value) {
        return Math.max(1, value);
    }

    private static int pageSize(int value) {
        return value <= 0 ? DEFAULT_PAGE_SIZE : Math.min(value, MAX_PAGE_SIZE);
    }

    private static int positiveOrDefault(int value, int defaultValue) {
        return value <= 0 ? defaultValue : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * 五类运营列表公共查询。
     */
    public record OperationQuery(
            Long ownerId,
            Long warehouseId,
            String sku,
            String batchNo,
            Integer status,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection) {

        public OperationQuery(
                Long ownerId,
                Long warehouseId,
                String sku,
                String batchNo,
                Integer status,
                int pageNo,
                int pageSize) {
            this(
                    ownerId, warehouseId, sku, batchNo, status, pageNo, pageSize,
                    "updatedAt", "desc");
        }
    }

    /**
     * 库存指标查询。
     */
    public record MetricQuery(
            Long ownerId,
            Long warehouseId,
            String sku,
            String batchNo,
            int slowMovingDays,
            int expiryWarningDays,
            int pageNo,
            int pageSize) {
    }

    private record ScopeValues(boolean wildcard, Set<Long> values) {
    }

    @FunctionalInterface
    private interface PageLoader<T> {

        InventoryOperationReadModelPort.Page<T> load(
                InventoryOperationReadModelPort.QueryScope scope,
                InventoryOperationReadModelPort.OperationFilter filter);
    }
}
