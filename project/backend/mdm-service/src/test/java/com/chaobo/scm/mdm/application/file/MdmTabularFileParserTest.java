package com.chaobo.scm.mdm.application.file;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdmTabularFileParserTest {

    private final MdmTabularFileParser parser = new MdmTabularFileParser();

    @Test
    void parsesQuotedCsvWithStableHeaders() {
        byte[] content = ("dataCode,dataName,remark\r\n"
                + "SKU-1,商品一,普通\r\n"
                + "SKU-2,\"商品,二\",\"含\"\"引号\"\"\"").getBytes(StandardCharsets.UTF_8);

        var rows = parser.parse("sku.csv", content);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(1)).containsEntry("dataName", "商品,二")
                .containsEntry("remark", "含\"引号\"");
    }

    @Test
    void rejectsUnsupportedFiles() {
        assertThatThrownBy(() -> parser.parse("sku.xls", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CSV");
    }

    @Test
    void parsesFirstWorksheetFromXlsx() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            writeEntry(zip, "xl/sharedStrings.xml",
                    "<sst><si><t>dataCode</t></si><si><t>dataName</t></si>"
                            + "<si><t>SKU-1</t></si><si><t>商品一</t></si></sst>");
            writeEntry(zip, "xl/worksheets/sheet1.xml",
                    "<worksheet><sheetData><row r=\"1\"><c r=\"A1\" t=\"s\"><v>0</v></c>"
                            + "<c r=\"B1\" t=\"s\"><v>1</v></c></row><row r=\"2\">"
                            + "<c r=\"A2\" t=\"s\"><v>2</v></c><c r=\"B2\" t=\"s\"><v>3</v></c>"
                            + "</row></sheetData></worksheet>");
        }

        var rows = parser.parse("sku.xlsx", bytes.toByteArray());

        assertThat(rows).singleElement().satisfies(row -> assertThat(row)
                .containsEntry("dataCode", "SKU-1")
                .containsEntry("dataName", "商品一"));
    }

    @Test
    void rejectsXlsxColumnReferencesBeyondTheColumnLimit() throws Exception {
        byte[] content = xlsx(null,
                "<worksheet><sheetData><row r=\"1\"><c r=\"IW1\" t=\"inlineStr\">"
                        + "<is><t>dataCode</t></is></c></row></sheetData></worksheet>");

        assertThatThrownBy(() -> parser.parse("malicious-column.xlsx", content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XLSX 列引用超过上限");
    }

    @Test
    void rejectsOversizedInlineCellValuesBeforeBuildingRows() throws Exception {
        String oversizedValue = "x".repeat(32768);
        byte[] content = xlsx(null,
                "<worksheet><sheetData><row r=\"1\"><c r=\"A1\" t=\"inlineStr\">"
                        + "<is><t>" + oversizedValue + "</t></is></c></row></sheetData></worksheet>");

        assertThatThrownBy(() -> parser.parse("oversized-cell.xlsx", content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("单元格内容超过上限");
    }

    @Test
    void rejectsOversizedSharedStringValuesBeforeResolvingCells() throws Exception {
        String oversizedValue = "x".repeat(32768);
        byte[] content = xlsx("<sst><si><t>" + oversizedValue + "</t></si></sst>",
                "<worksheet><sheetData><row r=\"1\"><c r=\"A1\" t=\"s\"><v>0</v></c>"
                        + "</row></sheetData></worksheet>");

        assertThatThrownBy(() -> parser.parse("oversized-shared-string.xlsx", content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("共享字符串内容超过上限");
    }

    @Test
    void rejectsTooManySharedStringsBeforeBuildingTheSharedStringTable() throws Exception {
        StringBuilder sharedStrings = new StringBuilder("<sst>");
        for (int index = 0; index <= 100000; index++) {
            sharedStrings.append("<si><t>x</t></si>");
        }
        sharedStrings.append("</sst>");
        byte[] content = xlsx(sharedStrings.toString(),
                "<worksheet><sheetData><row r=\"1\"><c r=\"A1\" t=\"s\"><v>0</v></c>"
                        + "</row></sheetData></worksheet>");

        assertThatThrownBy(() -> parser.parse("too-many-shared-strings.xlsx", content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("共享字符串数量超过上限");
    }

    private byte[] xlsx(String sharedStrings, String sheet) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            if (sharedStrings != null) {
                writeEntry(zip, "xl/sharedStrings.xml", sharedStrings);
            }
            writeEntry(zip, "xl/worksheets/sheet1.xml", sheet);
        }
        return bytes.toByteArray();
    }

    private void writeEntry(ZipOutputStream zip, String name, String content) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
