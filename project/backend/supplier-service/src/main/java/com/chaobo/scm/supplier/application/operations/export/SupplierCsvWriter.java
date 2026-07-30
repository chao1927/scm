package com.chaobo.scm.supplier.application.operations.export;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 按稳定列定义生成可被 Excel 正确识别的 UTF-8 CSV 文件。
 *
 * <p>输出固定携带 UTF-8 BOM、使用 CRLF 分隔记录，并按 RFC 4180 对逗号、双引号和换行做转义。
 * 对以公式控制字符开头的文本增加单引号，避免导出内容在电子表格中被当作公式执行。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierCsvWriter {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String DANGEROUS_FORMULA_PREFIXES = "=+-@\t\r";
    private static final char COMMA = ',';
    private static final char DOUBLE_QUOTE = '"';
    private static final char CARRIAGE_RETURN = '\r';
    private static final char LINE_FEED = '\n';

    /**
     * 生成完整 CSV 字节。
     *
     * @param columns 固定列定义，列表顺序就是文件列顺序
     * @param rows 数据行
     * @return UTF-8 BOM 编码的 CSV 字节
     */
    public byte[] write(List<Column> columns, List<? extends Map<String, ?>> rows) {
        var output = new ByteArrayOutputStream();
        output.writeBytes(UTF8_BOM);
        appendLine(output, columns.stream().map(Column::header).toList());
        for (var row : rows) {
            appendLine(output, columns.stream().map(column -> format(row.get(column.key()))).toList());
        }
        return output.toByteArray();
    }

    private void appendLine(ByteArrayOutputStream output, List<String> cells) {
        output.writeBytes(String.join(",", cells.stream().map(this::escape).toList())
                .concat("\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (value instanceof CharSequence && !text.isEmpty()
                && DANGEROUS_FORMULA_PREFIXES.indexOf(text.charAt(0)) >= 0) {
            return "'" + text;
        }
        return text;
    }

    private String escape(String value) {
        if (value.indexOf(COMMA) >= 0 || value.indexOf(DOUBLE_QUOTE) >= 0
                || value.indexOf(CARRIAGE_RETURN) >= 0 || value.indexOf(LINE_FEED) >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    /**
     * 定义一个不可变导出列。
     *
     * @param key 数据行键
     * @param header 中文表头
     */
    public record Column(String key, String header) {
        public Column {
            if (key == null || key.isBlank() || header == null || header.isBlank()) {
                throw new IllegalArgumentException("CSV 列键和表头不能为空");
            }
        }
    }
}
