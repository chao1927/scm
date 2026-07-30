package com.chaobo.scm.supplier.application.operations.export;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证供应商导出 CSV 的稳定列顺序、编码和转义契约。
 */
class SupplierCsvWriterTest {

    @Test
    void writesUtf8BomCrlfAndEscapesDangerousCells() {
        var columns = List.of(
                new SupplierCsvWriter.Column("name", "供应商名称"),
                new SupplierCsvWriter.Column("remark", "备注")
        );
        var rows = List.<Map<String, ?>>of(
                Map.of("name", "甲,乙", "remark", "他说\"好\"\n第二行"),
                Map.of("name", "=1+1", "remark", "正常")
        );

        byte[] content = new SupplierCsvWriter().write(columns, rows);

        assertThat(content).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(new String(content, StandardCharsets.UTF_8))
                .isEqualTo("\uFEFF供应商名称,备注\r\n\"甲,乙\",\"他说\"\"好\"\"\n第二行\"\r\n'=1+1,正常\r\n");
    }

    @Test
    void writesNullAsEmptyCellAndKeepsDeclaredColumnOrder() {
        var columns = List.of(
                new SupplierCsvWriter.Column("second", "第二列"),
                new SupplierCsvWriter.Column("first", "第一列")
        );
        var row = new java.util.HashMap<String, Object>();
        row.put("first", "一");
        row.put("second", null);

        var csv = new String(new SupplierCsvWriter().write(columns, List.of(row)), StandardCharsets.UTF_8);

        assertThat(csv).isEqualTo("\uFEFF第二列,第一列\r\n,一\r\n");
    }
}
