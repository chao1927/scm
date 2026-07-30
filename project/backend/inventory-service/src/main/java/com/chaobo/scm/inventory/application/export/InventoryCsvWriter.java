package com.chaobo.scm.inventory.application.export;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 生成固定列、可防电子表格公式注入的 UTF-8 CSV。
 *
 * @author SCM Team
 */
public final class InventoryCsvWriter {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String DANGEROUS_PREFIXES = "=+-@\t\r";

    public byte[] write(List<Column> columns, List<? extends Map<String, ?>> rows) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.writeBytes(UTF8_BOM);
        append(output, columns.stream().map(Column::header).toList());
        for (Map<String, ?> row : rows) {
            append(output, columns.stream()
                    .map(column -> format(row.get(column.key())))
                    .toList());
        }
        return output.toByteArray();
    }

    private void append(ByteArrayOutputStream output, List<String> values) {
        output.writeBytes(String.join(",", values.stream().map(this::escape).toList())
                .concat("\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (value instanceof CharSequence
                && !text.isEmpty()
                && DANGEROUS_PREFIXES.indexOf(text.charAt(0)) >= 0) {
            return "'" + text;
        }
        return text;
    }

    private String escape(String value) {
        if (value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\r') >= 0
                || value.indexOf('\n') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    public record Column(String key, String header) {

        public Column {
            if (key == null || key.isBlank() || header == null || header.isBlank()) {
                throw new IllegalArgumentException("CSV 列键和表头不能为空");
            }
        }
    }
}
