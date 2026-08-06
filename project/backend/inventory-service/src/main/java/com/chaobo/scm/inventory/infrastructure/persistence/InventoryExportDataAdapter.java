package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryOperationReadModelPort;
import com.chaobo.scm.inventory.application.export.InventoryExportDataPort;
import com.chaobo.scm.inventory.application.export.InventoryExportTask;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * 使用运营只读模型加载导出数据，确保页面和导出口径一致。
 *
 * @author SCM Team
 */
@Repository
public class InventoryExportDataAdapter implements InventoryExportDataPort {

    private static final String WILDCARD = "*";
    private static final TypeReference<List<String>> STRING_LIST =
            new TypeReference<>() { };
    private static final TypeReference<Map<String, Object>> OBJECT_MAP =
            new TypeReference<>() { };
    private final InventoryOperationReadModelPort readModel;
    private final ObjectMapper json;

    public InventoryExportDataAdapter(
            InventoryOperationReadModelPort readModel,
            ObjectMapper json) {
        this.readModel = readModel;
        this.json = json;
    }

    @Override
    public List<Map<String, Object>> load(
            InventoryExportTask task,
            int maxRows) {
        InventoryOperationReadModelPort.QueryScope scope = scope(task);
        Map<String, Object> query = map(task.queryJson());
        String sku = text(query.get("sku"));
        String batchNo = text(query.get("batchNo"));
        Integer status = integer(query.get("status"));
        InventoryOperationReadModelPort.OperationFilter operations =
                new InventoryOperationReadModelPort.OperationFilter(
                        sku, batchNo, status, 0, maxRows, "updated_at desc");
        InventoryOperationReadModelPort.Page<?> page = switch (task.exportType()) {
            case "RESERVATION" -> readModel.reservations(scope, operations);
            case "FREEZE" -> readModel.freezes(scope, operations);
            case "ADJUSTMENT" -> readModel.adjustments(scope, operations);
            case "EVENT_LOG" -> readModel.eventLogs(scope, operations);
            case "OPERATION_LOG" -> readModel.operationLogs(scope, operations);
            case "BOOK_PHYSICAL", "STOCK_AGE", "SLOW_MOVING", "EXPIRY" ->
                    readModel.metrics(
                            InventoryOperationReadModelPort.MetricType.valueOf(task.exportType()),
                            scope,
                            new InventoryOperationReadModelPort.MetricFilter(
                                    sku,
                                    batchNo,
                                    positive(query.get("slowMovingDays"), 90),
                                    positive(query.get("expiryWarningDays"), 30),
                                    0,
                                    maxRows));
            default -> throw new IllegalArgumentException("不支持的库存导出类型");
        };
        if (page.total() > maxRows) {
            throw new IllegalStateException(
                    "导出数据量超过上限 " + maxRows + "，请缩小查询范围");
        }
        return page.records().stream()
                .map(row -> json.convertValue(row, OBJECT_MAP))
                .toList();
    }

    private InventoryOperationReadModelPort.QueryScope scope(
            InventoryExportTask task) {
        List<String> owners = strings(task.ownerScopeJson());
        List<String> warehouses = strings(task.warehouseScopeJson());
        return new InventoryOperationReadModelPort.QueryScope(
                owners.contains(WILDCARD),
                ids(owners),
                warehouses.contains(WILDCARD),
                ids(warehouses));
    }

    private List<String> strings(String value) {
        try {
            return json.readValue(value, STRING_LIST);
        } catch (JacksonException exception) {
            throw new IllegalStateException("导出数据范围快照损坏", exception);
        }
    }

    private Map<String, Object> map(String value) {
        try {
            return json.readValue(value, OBJECT_MAP);
        } catch (JacksonException exception) {
            throw new IllegalStateException("导出查询条件损坏", exception);
        }
    }

    private static Set<Long> ids(List<String> values) {
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        for (String value : values) {
            if (!WILDCARD.equals(value)) {
                result.add(Long.parseLong(value));
            }
        }
        return Set.copyOf(result);
    }

    private static String text(Object value) {
        return value == null || value.toString().isBlank()
                ? null
                : value.toString().trim();
    }

    private static Integer integer(Object value) {
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private static int positive(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        int parsed = Integer.parseInt(value.toString());
        return parsed > 0 ? parsed : fallback;
    }
}
