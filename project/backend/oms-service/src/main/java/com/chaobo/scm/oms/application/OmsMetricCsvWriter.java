package com.chaobo.scm.oms.application;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** 生成带 UTF-8 BOM、RFC 4180 转义和公式注入防护的履约指标 CSV。 */
public final class OmsMetricCsvWriter {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String DANGEROUS_PREFIXES = "=+-@\t\r";
    private static final List<String> HEADERS = List.of(
            "记录类型", "订单号", "组织ID", "货主ID", "仓库ID", "订单创建时间",
            "履约结果", "完成时间", "履约时长秒", "履约单数", "订单量", "完成量",
            "取消量", "履约率", "平均履约时长秒", "口径说明", "事实来源");

    public byte[] write(OmsFulfillmentMetricsApplicationService.MetricResult result) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.writeBytes(UTF8_BOM);
        line(output, HEADERS);
        var summary = result.summary();
        line(output, List.of("SUMMARY", "", "", "", "", "", "", "", "", "",
                String.valueOf(summary.orderCount()),
                String.valueOf(summary.completedOrderCount()),
                String.valueOf(summary.cancelledOrderCount()),
                summary.fulfillmentRate().toPlainString(),
                value(summary.averageFulfillmentDurationSeconds()),
                summary.orderCountDefinition() + "；" + summary.completedDefinition()
                        + "；" + summary.cancelledDefinition() + "；"
                        + summary.fulfillmentRateDefinition() + "；" + summary.durationDefinition(),
                String.join(",", summary.sourceTables())));
        for (var row : result.rows()) {
            List<String> cells = new ArrayList<>(HEADERS.size());
            cells.add("ORDER");
            cells.add(row.orderNo());
            cells.add(value(row.organizationId()));
            cells.add(value(row.ownerId()));
            cells.add(row.warehouseIds());
            cells.add(value(row.orderCreatedAt()));
            cells.add(row.outcome());
            cells.add(value(row.completedAt()));
            cells.add(value(row.fulfillmentDurationSeconds()));
            cells.add(String.valueOf(row.fulfillmentCount()));
            while (cells.size() < HEADERS.size()) {
                cells.add("");
            }
            line(output, cells);
        }
        return output.toByteArray();
    }

    private void line(ByteArrayOutputStream output, List<String> cells) {
        output.writeBytes(String.join(",", cells.stream().map(this::escape).toList())
                .concat("\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String input) {
        String value = input == null ? "" : input;
        if (!value.isEmpty() && DANGEROUS_PREFIXES.indexOf(value.charAt(0)) >= 0) {
            value = "'" + value;
        }
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
