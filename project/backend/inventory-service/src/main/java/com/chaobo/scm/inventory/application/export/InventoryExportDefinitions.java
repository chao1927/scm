package com.chaobo.scm.inventory.application.export;

import java.util.List;
import java.util.Map;

/**
 * 库存导出类型与稳定列定义。
 *
 * @author SCM Team
 */
public final class InventoryExportDefinitions {

    private static final List<InventoryCsvWriter.Column> OPERATION_COLUMNS = columns(
            "ownerId", "货主ID", "warehouseId", "仓库ID", "sku", "SKU",
            "batchNo", "批次号", "status", "状态", "updatedAt", "更新时间");
    private static final List<InventoryCsvWriter.Column> METRIC_COLUMNS = columns(
            "ownerId", "货主ID", "warehouseId", "仓库ID", "sku", "SKU",
            "batchNo", "批次号", "bookQty", "账面数量", "physicalQty", "实盘数量",
            "differenceQty", "差异数量", "stockAgeDays", "库龄天数",
            "inactiveDays", "未动销天数", "expiryDate", "效期",
            "daysToExpiry", "剩余天数", "basis", "指标口径", "factAt", "事实时间");
    private static final Map<String, List<InventoryCsvWriter.Column>> DEFINITIONS = Map.ofEntries(
            Map.entry("RESERVATION", OPERATION_COLUMNS),
            Map.entry("FREEZE", OPERATION_COLUMNS),
            Map.entry("ADJUSTMENT", OPERATION_COLUMNS),
            Map.entry("EVENT_LOG", OPERATION_COLUMNS),
            Map.entry("OPERATION_LOG", OPERATION_COLUMNS),
            Map.entry("BOOK_PHYSICAL", METRIC_COLUMNS),
            Map.entry("STOCK_AGE", METRIC_COLUMNS),
            Map.entry("SLOW_MOVING", METRIC_COLUMNS),
            Map.entry("EXPIRY", METRIC_COLUMNS));

    private InventoryExportDefinitions() {
    }

    public static boolean supports(String type) {
        return type != null && DEFINITIONS.containsKey(type);
    }

    public static List<InventoryCsvWriter.Column> columnsFor(String type) {
        List<InventoryCsvWriter.Column> columns = DEFINITIONS.get(type);
        if (columns == null) {
            throw new IllegalArgumentException("不支持的库存导出类型");
        }
        return columns;
    }

    private static List<InventoryCsvWriter.Column> columns(String... pairs) {
        java.util.ArrayList<InventoryCsvWriter.Column> result =
                new java.util.ArrayList<>(pairs.length / 2);
        for (int index = 0; index < pairs.length; index += 2) {
            result.add(new InventoryCsvWriter.Column(pairs[index], pairs[index + 1]));
        }
        return List.copyOf(result);
    }
}
