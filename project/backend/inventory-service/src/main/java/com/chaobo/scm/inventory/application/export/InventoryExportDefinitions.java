package com.chaobo.scm.inventory.application.export;

import java.util.List;
import java.util.Map;

/**
 * 库存导出类型与稳定列定义。
 *
 * @author SCM Team
 */
public final class InventoryExportDefinitions {

    private static final List<InventoryCsvWriter.Column> METRIC_COLUMNS = columns(
            "ownerId", "货主ID", "warehouseId", "仓库ID", "sku", "SKU",
            "batchNo", "批次号", "bookQty", "账面数量", "physicalQty", "实盘数量",
            "differenceQty", "差异数量", "stockAgeDays", "库龄天数",
            "inactiveDays", "未动销天数", "expiryDate", "效期",
            "daysToExpiry", "剩余天数", "basis", "指标口径", "factAt", "事实时间");
    private static final Map<String, List<InventoryCsvWriter.Column>> DEFINITIONS = Map.ofEntries(
            Map.entry("RESERVATION", columns(
                    "reservationNo", "预占单号", "ownerId", "货主ID",
                    "warehouseId", "仓库ID", "sku", "SKU", "batchNo", "批次号",
                    "sourceSystem", "来源系统", "sourceNo", "来源单号",
                    "reservedQty", "预占数量", "releasedQty", "已释放数量",
                    "status", "状态", "version", "版本", "updatedAt", "更新时间")),
            Map.entry("FREEZE", columns(
                    "freezeNo", "冻结单号", "ownerId", "货主ID",
                    "warehouseId", "仓库ID", "sku", "SKU", "batchNo", "批次号",
                    "freezeQty", "冻结数量", "unfrozenQty", "已解冻数量",
                    "reason", "冻结原因", "status", "状态",
                    "approvalStatus", "审批状态", "version", "版本",
                    "updatedAt", "更新时间")),
            Map.entry("ADJUSTMENT", columns(
                    "adjustmentNo", "调整单号", "ownerId", "货主ID",
                    "warehouseId", "仓库ID", "sku", "SKU", "batchNo", "批次号",
                    "adjustQty", "调整数量", "adjustmentType", "调整类型",
                    "reason", "调整原因", "status", "状态",
                    "approvalStatus", "审批状态", "version", "版本",
                    "updatedAt", "更新时间")),
            Map.entry("EVENT_LOG", columns(
                    "direction", "方向", "sourceSystem", "来源系统",
                    "eventCode", "事件编码", "eventType", "事件类型",
                    "eventVersion", "版本", "ownerId", "货主ID",
                    "warehouseId", "仓库ID", "aggregateType", "聚合类型",
                    "aggregateId", "聚合标识", "status", "状态",
                    "retryCount", "重试次数", "lastError", "最近错误",
                    "updatedAt", "更新时间")),
            Map.entry("OPERATION_LOG", columns(
                    "logId", "日志ID", "ownerId", "货主ID",
                    "warehouseId", "仓库ID", "operatorId", "操作人ID",
                    "operationType", "操作类型", "operationReason", "操作原因",
                    "targetType", "对象类型", "targetNo", "对象编号",
                    "result", "结果", "requestId", "请求ID",
                    "operationAt", "操作时间")),
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
