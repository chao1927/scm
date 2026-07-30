package com.chaobo.scm.supplier.application.operations.export;

import java.util.List;
import java.util.Map;

/**
 * 集中维护各类供应商 CSV 的稳定列契约。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierExportDefinitions {

    private static final int COLUMN_PAIR_SIZE = 2;
    private static final Map<String, List<SupplierCsvWriter.Column>> DEFINITIONS = Map.of(
            "WORK_ITEM", columns("workItemId", "待办ID", "workType", "待办类型", "supplierId", "供应商ID",
                    "businessType", "业务类型", "businessNo", "业务单号", "title", "标题", "status", "状态",
                    "dueAt", "截止时间", "updatedAt", "更新时间"),
            "WARNING", columns("warningId", "预警ID", "warningType", "预警类型", "supplierId", "供应商ID",
                    "businessType", "业务类型", "warningLevel", "预警级别", "warningMessage", "预警内容",
                    "status", "状态", "occurredAt", "发生时间"),
            "FAILED_EVENT", columns("direction", "方向", "eventId", "事件ID", "eventType", "事件类型",
                    "eventCode", "事件编码", "status", "状态", "failureReason", "失败原因", "updatedAt", "更新时间"),
            "RECONCILIATION", columns("reconciliationJobId", "对账任务ID", "reconciliationType", "对账类型",
                    "targetSystem", "目标系统", "businessDate", "业务日期", "localCount", "本地数量",
                    "remoteCount", "对方数量", "differenceDetail", "差异说明", "status", "状态"),
            "SCORE", columns("scoreResultId", "评分ID", "supplierId", "供应商ID", "periodCode", "周期",
                    "totalScore", "总分", "manualAdjustment", "人工调整", "status", "状态", "publishedAt", "发布时间"),
            "QUALITY", columns("qualityIssueId", "质量问题ID", "issueNo", "问题编号", "supplierId", "供应商ID",
                    "sourceType", "来源类型", "sourceNo", "来源单号", "severity", "严重度",
                    "issueDescription", "问题描述", "issueStatus", "状态", "rectificationDeadline", "整改期限"),
            "RETURN", columns("returnId", "退供ID", "returnNo", "退供单号", "supplierId", "供应商ID",
                    "warehouseId", "仓库ID", "returnReason", "退供原因", "returnStatus", "状态",
                    "waybillNo", "运单号", "offsetAmount", "冲减金额", "claimAmount", "索赔金额")
    );

    private SupplierExportDefinitions() {
    }

    public static List<SupplierCsvWriter.Column> columnsFor(String exportType) {
        var columns = DEFINITIONS.get(exportType);
        if (columns == null) {
            throw new IllegalArgumentException("不支持的导出类型: " + exportType);
        }
        return columns;
    }

    public static boolean supports(String exportType) {
        return exportType != null && DEFINITIONS.containsKey(exportType);
    }

    private static List<SupplierCsvWriter.Column> columns(String... pairs) {
        var result = new java.util.ArrayList<SupplierCsvWriter.Column>(pairs.length / COLUMN_PAIR_SIZE);
        for (int index = 0; index < pairs.length; index += COLUMN_PAIR_SIZE) {
            result.add(new SupplierCsvWriter.Column(pairs[index], pairs[index + 1]));
        }
        return List.copyOf(result);
    }
}
