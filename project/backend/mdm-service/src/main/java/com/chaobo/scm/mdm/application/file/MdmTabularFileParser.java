package com.chaobo.scm.mdm.application.file;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * CSV/XLSX 表格解析器。
 *
 * <p>只负责把文件转换为按表头命名的行，不在基础设施解析阶段写入任何主数据。
 */
@Component
public class MdmTabularFileParser {

    private static final int MAX_ZIP_ENTRIES = 100;
    private static final int MAX_UNCOMPRESSED_BYTES = 20 * 1024 * 1024;
    private static final int MAX_ROWS = 100000;
    private static final int MAX_COLUMNS = 256;
    private static final int MAX_CELL_CHARACTERS = 32767;
    private static final int MAX_SHARED_STRINGS = 100000;

    public List<Map<String, String>> parse(String fileName, byte[] content) {
        String lowerName = fileName == null ? "" : fileName.toLowerCase(java.util.Locale.ROOT);
        if (lowerName.endsWith(".csv")) {
            return parseCsv(content);
        }
        if (lowerName.endsWith(".xlsx")) {
            return parseXlsx(content);
        }
        throw new IllegalArgumentException("仅支持 CSV 或 XLSX 文件");
    }

    private List<Map<String, String>> parseCsv(byte[] content) {
        String text = new String(content, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }
        List<List<String>> records = new ArrayList<>();
        List<String> currentRecord = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < text.length() && text.charAt(index + 1) == '"') {
                    currentCell.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                currentRecord.add(currentCell.toString());
                currentCell.setLength(0);
            } else if ((character == '\n' || character == '\r') && !quoted) {
                if (character == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                currentRecord.add(currentCell.toString());
                currentCell.setLength(0);
                if (!currentRecord.stream().allMatch(String::isBlank)) {
                    records.add(currentRecord);
                }
                currentRecord = new ArrayList<>();
            } else {
                currentCell.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("CSV 引号未闭合");
        }
        if (currentCell.length() > 0 || !currentRecord.isEmpty()) {
            currentRecord.add(currentCell.toString());
            records.add(currentRecord);
        }
        return toRows(records);
    }

    private List<Map<String, String>> parseXlsx(byte[] content) {
        try {
            Map<String, byte[]> entries = unzip(content);
            List<String> sharedStrings = parseSharedStrings(entries.get("xl/sharedStrings.xml"));
            byte[] sheet = entries.get("xl/worksheets/sheet1.xml");
            if (sheet == null) {
                throw new IllegalArgumentException("XLSX 不包含第一个工作表");
            }
            Document document = secureFactory().newDocumentBuilder().parse(new ByteArrayInputStream(sheet));
            NodeList rowNodes = document.getElementsByTagName("row");
            if (rowNodes.getLength() > MAX_ROWS + 1) {
                throw new IllegalArgumentException("导入文件行数超过上限");
            }
            List<List<String>> records = new ArrayList<>();
            for (int rowIndex = 0; rowIndex < rowNodes.getLength(); rowIndex++) {
                Element row = (Element) rowNodes.item(rowIndex);
                NodeList cells = row.getElementsByTagName("c");
                if (cells.getLength() > MAX_COLUMNS) {
                    throw new IllegalArgumentException("XLSX 行单元格数量超过上限");
                }
                List<String> record = new ArrayList<>();
                for (int cellIndex = 0; cellIndex < cells.getLength(); cellIndex++) {
                    Element cell = (Element) cells.item(cellIndex);
                    int columnIndex = columnIndex(cell.getAttribute("r"));
                    while (record.size() < columnIndex) {
                        record.add("");
                    }
                    String value = cellValue(cell, sharedStrings);
                    if (record.size() == columnIndex) {
                        record.add(value);
                    } else {
                        record.set(columnIndex, value);
                    }
                }
                records.add(record);
            }
            return toRows(records);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("XLSX 文件无法解析", exception);
        }
    }

    private Map<String, byte[]> unzip(byte[] content) throws java.io.IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (entries.size() >= MAX_ZIP_ENTRIES) {
                        throw new IllegalArgumentException("XLSX 条目数量超过上限");
                    }
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int total = entries.values().stream().mapToInt(bytes -> bytes.length).sum();
                    int read;
                    while ((read = input.read(buffer)) >= 0) {
                        total += read;
                        if (total > MAX_UNCOMPRESSED_BYTES) {
                            throw new IllegalArgumentException("XLSX 解压内容超过上限");
                        }
                        output.write(buffer, 0, read);
                    }
                    entries.put(entry.getName(), output.toByteArray());
                }
            }
        }
        return entries;
    }

    private List<String> parseSharedStrings(byte[] content) throws Exception {
        if (content == null) {
            return List.of();
        }
        Document document = secureFactory().newDocumentBuilder().parse(new ByteArrayInputStream(content));
        NodeList nodes = document.getElementsByTagName("si");
        if (nodes.getLength() > MAX_SHARED_STRINGS) {
            throw new IllegalArgumentException("XLSX 共享字符串数量超过上限");
        }
        List<String> values = new ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            values.add(requireCellLength(nodes.item(index).getTextContent(), "XLSX 共享字符串内容超过上限"));
        }
        return values;
    }

    private DocumentBuilderFactory secureFactory() throws javax.xml.parsers.ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private String cellValue(Element cell, List<String> sharedStrings) {
        NodeList values = cell.getElementsByTagName("v");
        if (values.getLength() == 0) {
            NodeList inline = cell.getElementsByTagName("t");
            return inline.getLength() == 0 ? "" : requireCellLength(inline.item(0).getTextContent(),
                    "XLSX 单元格内容超过上限");
        }
        String raw = requireCellLength(values.item(0).getTextContent(), "XLSX 单元格内容超过上限");
        if ("s".equals(cell.getAttribute("t"))) {
            int index = Integer.parseInt(raw);
            return index >= 0 && index < sharedStrings.size() ? sharedStrings.get(index) : "";
        }
        return raw;
    }

    private int columnIndex(String reference) {
        long value = 0;
        int index = 0;
        while (index < reference.length() && Character.isLetter(reference.charAt(index))) {
            value = value * 26 + Character.toUpperCase(reference.charAt(index)) - 'A' + 1;
            if (value > MAX_COLUMNS) {
                throw new IllegalArgumentException("XLSX 列引用超过上限");
            }
            index++;
        }
        return (int) Math.max(0, value - 1);
    }

    private String requireCellLength(String value, String message) {
        String safeValue = value == null ? "" : value;
        if (safeValue.length() > MAX_CELL_CHARACTERS) {
            throw new IllegalArgumentException(message);
        }
        return safeValue;
    }

    private List<Map<String, String>> toRows(List<List<String>> records) {
        if (records.isEmpty()) {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        if (records.size() > MAX_ROWS + 1) {
            throw new IllegalArgumentException("导入文件行数超过上限");
        }
        List<String> headers = records.get(0).stream().map(String::trim).toList();
        if (headers.size() > MAX_COLUMNS) {
            throw new IllegalArgumentException("导入文件列数超过上限");
        }
        if (headers.isEmpty() || headers.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("导入文件表头不能为空");
        }
        if (headers.stream().distinct().count() != headers.size()) {
            throw new IllegalArgumentException("导入文件表头不能重复");
        }
        List<Map<String, String>> rows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < records.size(); rowIndex++) {
            List<String> cells = records.get(rowIndex);
            Map<String, String> row = new LinkedHashMap<>();
            for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                row.put(headers.get(columnIndex), columnIndex < cells.size() ? cells.get(columnIndex) : "");
            }
            if (!row.values().stream().allMatch(String::isBlank)) {
                rows.add(row);
            }
        }
        return rows;
    }
}
