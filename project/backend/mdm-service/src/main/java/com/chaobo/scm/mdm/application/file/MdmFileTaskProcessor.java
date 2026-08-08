package com.chaobo.scm.mdm.application.file;

import com.chaobo.scm.common.logging.ScmLogContext;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编排单个主数据导入或导出文件任务。
 */
@Service
public class MdmFileTaskProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MdmFileTaskProcessor.class);

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private static final java.util.Set<String> SAFE_PLAIN_EXPORT_FIELDS =
            java.util.Set.of("dataCode", "dataName", "status");
    private final MdmImportQualityMapper taskMapper;
    private final MasterDataRecordMapper recordMapper;
    private final MdmFileStoragePort storage;
    private final MdmTabularFileParser parser;
    private final MdmFileTaskExecutionService execution;
    private final int retryDelaySeconds;
    private final int exportMaxRows;

    public MdmFileTaskProcessor(MdmImportQualityMapper taskMapper,
                                MasterDataRecordMapper recordMapper,
                                MdmFileStoragePort storage,
                                MdmTabularFileParser parser,
                                MdmFileTaskExecutionService execution,
                                @Value("${scm.mdm.file-task.retry-delay-seconds:60}") int retryDelaySeconds,
                                @Value("${scm.mdm.file-task.export-max-rows:10000}") int exportMaxRows) {
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.storage = storage;
        this.parser = parser;
        this.execution = execution;
        this.retryDelaySeconds = retryDelaySeconds;
        this.exportMaxRows = exportMaxRows;
    }

    public void processImport(MdmImportQualityMapper.ImportTaskRow task) {
        if (taskMapper.claimImportTask(task.importTaskNo(), task.version()) != 1) {
            return;
        }
        try (ScmLogContext ignored = ScmLogContext.openSystem(task.importTaskNo())) {
            if (task.status() == com.chaobo.scm.mdm.domain.ImportTaskAggregate.VALIDATED) {
                execution.applyValidatedRows(task.importTaskNo());
                taskMapper.releaseImportTask(task.importTaskNo());
                LOG.info("event=batch_task operation=mdm_import_apply result=SUCCESS taskNo={}",
                        task.importTaskNo());
                return;
            }
            MdmFileStoragePort.StoredContent source = storage.load(task.fileUrl());
            List<Map<String, String>> rows = parser.parse(task.fileName(), source.bytes());
            execution.stageAndValidate(task, source.bytes(), rows);
            MdmImportQualityMapper.ImportTaskRow validated = taskMapper.findImportTask(task.importTaskNo());
            if (!validated.validateOnly() && validated.successCount() > 0) {
                execution.applyValidatedRows(task.importTaskNo());
            }
            taskMapper.releaseImportTask(task.importTaskNo());
            LOG.info("event=batch_task operation=mdm_import result=SUCCESS taskNo={}",
                    task.importTaskNo());
        } catch (RuntimeException exception) {
            taskMapper.failImportProcessing(task.importTaskNo(), failureReason(exception), retryDelaySeconds);
            try (ScmLogContext ignored = ScmLogContext.openSystem(task.importTaskNo())) {
                LOG.error("event=batch_task operation=mdm_import result=FAILURE taskNo={}",
                        task.importTaskNo(), exception);
            }
        }
    }

    public void processExport(MdmImportQualityMapper.ExportTaskRow task) {
        if (taskMapper.claimExportTask(task.exportTaskNo(), task.version()) != 1) {
            return;
        }
        try (ScmLogContext ignored = ScmLogContext.openSystem(task.exportTaskNo())) {
            ExportSpec spec = ExportSpec.parse(task, new com.fasterxml.jackson.databind.ObjectMapper());
            List<MasterDataRecordMapper.RecordRow> rows = recordMapper.listRecordsForExport(
                    task.typeCode(), spec.status(), spec.dataCodePrefix(), exportMaxRows + 1);
            if (rows.size() > exportMaxRows) {
                throw new IllegalStateException("导出结果超过上限，请缩小查询范围");
            }
            byte[] csv = exportCsv(rows, spec);
            String objectKey = "exports/" + task.exportTaskNo() + "/master-data.csv";
            storage.store(objectKey, csv, CSV_CONTENT_TYPE);
            taskMapper.completeExportTask(task.exportTaskNo(), objectKey);
            LOG.info("event=batch_task operation=mdm_export result=SUCCESS taskNo={} typeCode={} rowCount={}",
                    task.exportTaskNo(), task.typeCode(), rows.size());
        } catch (RuntimeException exception) {
            taskMapper.failExportTask(task.exportTaskNo(), failureReason(exception), retryDelaySeconds);
            try (ScmLogContext ignored = ScmLogContext.openSystem(task.exportTaskNo())) {
                LOG.error("event=batch_task operation=mdm_export result=FAILURE taskNo={} typeCode={}",
                        task.exportTaskNo(), task.typeCode(), exception);
            }
        }
    }

    private byte[] exportCsv(List<MasterDataRecordMapper.RecordRow> rows, ExportSpec spec) {
        List<String> fields = spec.fields();
        StringBuilder csv = new StringBuilder("\uFEFF").append(String.join(",", fields)).append("\r\n");
        for (MasterDataRecordMapper.RecordRow row : rows) {
            java.util.Map<String, Object> values = new java.util.LinkedHashMap<>();
            try {
                values.putAll(new com.fasterxml.jackson.databind.ObjectMapper().readValue(row.dataPayload(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() { }));
            } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
                throw new IllegalStateException("主数据载荷无法导出", exception);
            }
            // 基础事实字段必须以记录列为准，不允许 dataPayload 中的同名键覆盖。
            values.put("dataCode", row.dataCode());
            values.put("dataName", row.dataName());
            values.put("status", String.valueOf(row.status()));
            values.put("dataPayload", row.dataPayload());
            csv.append(fields.stream().map(field -> escape(spec.value(field, values)))
                    .collect(java.util.stream.Collectors.joining(","))).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        String safe = value == null ? "" : value;
        if (!safe.isEmpty() && "=+-@\t\r".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return '"' + safe.replace("\"", "\"\"") + '"';
    }

    private String failureReason(RuntimeException exception) {
        String message = exception.getMessage();
        String reason = exception.getClass().getSimpleName()
                + (message == null || message.isBlank() ? "" : ": " + message);
        return reason.length() <= 500 ? reason : reason.substring(0, 500);
    }

    private record ExportSpec(Integer status, String dataCodePrefix, List<String> fields,
                              boolean maskSensitiveFields) {
        static ExportSpec parse(MdmImportQualityMapper.ExportTaskRow task,
                                com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
            try {
                java.util.Map<String, Object> filter = task.filterPayload() == null || task.filterPayload().isBlank()
                        ? java.util.Map.of() : objectMapper.readValue(task.filterPayload(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() { });
                java.util.Set<String> unknownFilters = new java.util.LinkedHashSet<>(filter.keySet());
                unknownFilters.removeAll(java.util.Set.of("status", "dataCodePrefix"));
                if (!unknownFilters.isEmpty()) {
                    throw new IllegalArgumentException(
                        "unsupported export filter fields in task snapshot: " + unknownFilters);
                }
                Integer status = null;
                if (filter.get("status") != null) {
                    if (!(filter.get("status") instanceof Number number)
                            || number.doubleValue() != number.intValue()) {
                        throw new IllegalArgumentException(
                            "export task status filter must be an integer");
                    }
                    status = number.intValue();
                }
                String prefix = null;
                if (filter.get("dataCodePrefix") != null) {
                    if (!(filter.get("dataCodePrefix") instanceof String text)) {
                        throw new IllegalArgumentException(
                            "export task dataCodePrefix filter must be a string");
                    }
                    prefix = text.isBlank() ? null : text.trim();
                }
                if (task.fieldPayload() == null || task.fieldPayload().isBlank()) {
                    throw new IllegalArgumentException(
                        "export task authorization field snapshot is missing");
                }
                List<String> fields = objectMapper.readValue(task.fieldPayload(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() { });
                if (fields == null || fields.isEmpty()) {
                    throw new IllegalArgumentException(
                        "export task authorization field snapshot is empty");
                }
                java.util.LinkedHashSet<String> validated = new java.util.LinkedHashSet<>();
                for (String field : fields) {
                    if (field == null
                            || !field.matches("[A-Za-z][A-Za-z0-9_.-]{0,127}")) {
                        throw new IllegalArgumentException(
                            "export task authorization field snapshot is invalid");
                    }
                    if (!validated.add(field)) {
                        throw new IllegalArgumentException(
                            "export task authorization field snapshot contains duplicates");
                    }
                }
                return new ExportSpec(status, prefix == null || prefix.isBlank() ? null : prefix,
                        List.copyOf(validated), task.maskSensitiveFields());
            } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
                throw new IllegalArgumentException("导出过滤或字段配置非法", exception);
            }
        }

        String value(String field, java.util.Map<String, Object> values) {
            Object value = values.get(field);
            // Worker 只信任服务端固化的脱敏布尔决策：一旦要求脱敏，
            // 除三个安全基础字段外全部隐藏，不再依赖可被遗漏的字段名关键词。
            if (maskSensitiveFields && !SAFE_PLAIN_EXPORT_FIELDS.contains(field)) {
                return value == null ? "" : "***";
            }
            return value == null ? "" : String.valueOf(value);
        }
    }
}
