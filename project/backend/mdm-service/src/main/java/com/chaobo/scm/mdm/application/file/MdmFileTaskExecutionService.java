package com.chaobo.scm.mdm.application.file;

import com.chaobo.scm.mdm.application.MasterDataRecordApplicationService;
import com.chaobo.scm.mdm.application.MdmImportQualityApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 主数据文件任务的事务执行单元。
 *
 * <p>解析发生在事务外；暂存、错误记录和状态推进在短事务内完成。正式导入时所有主数据行处于同一事务，
 * 任意基础设施异常都会整体回滚，避免出现任务失败但部分数据已生效。
 */
@Service
public class MdmFileTaskExecutionService {

    private static final String SHA_256_PATTERN = "[0-9a-fA-F]{64}";
    private final MdmImportQualityMapper taskMapper;
    private final MasterDataRecordMapper recordMapper;
    private final MdmImportQualityApplicationService importService;
    private final MasterDataRecordApplicationService recordService;
    private final ObjectMapper objectMapper;
    private final MdmFileStoragePort storage;

    public MdmFileTaskExecutionService(MdmImportQualityMapper taskMapper,
                                       MasterDataRecordMapper recordMapper,
                                       MdmImportQualityApplicationService importService,
                                       MasterDataRecordApplicationService recordService,
                                       ObjectMapper objectMapper,
                                       MdmFileStoragePort storage) {
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.importService = importService;
        this.recordService = recordService;
        this.objectMapper = objectMapper;
        this.storage = storage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void stageAndValidate(MdmImportQualityMapper.ImportTaskRow task,
                                 byte[] source,
                                 List<Map<String, String>> rows) {
        verifyDigest(task.fileHash(), source);
        taskMapper.deleteImportErrors(task.importTaskNo());
        taskMapper.deleteImportStaging(task.importTaskNo());
        List<MdmImportQualityMapper.ImportErrorRow> errors = validate(task, rows);
        String errorFileUrl = null;
        if (!errors.isEmpty()) {
            errorFileUrl = "imports/" + task.importTaskNo() + "/errors.csv";
            storage.store(errorFileUrl, errorCsv(errors), "text/csv;charset=UTF-8");
        }
        Set<Integer> invalidRows = errors.stream().map(MdmImportQualityMapper.ImportErrorRow::rowNo)
                .collect(java.util.stream.Collectors.toSet());
        for (int index = 0; index < rows.size(); index++) {
            int rowNo = index + 2;
            Map<String, String> row = rows.get(index);
            if (!invalidRows.contains(rowNo)) {
                taskMapper.insertImportStaging(new MdmImportQualityMapper.ImportStagingRow(
                        task.importTaskNo(), rowNo, row.get("dataCode"), row.get("dataName"), toJson(row)));
            }
        }
        importService.validateImportTask(task.importTaskNo(),
                new MdmImportQualityApplicationService.ValidateImportTaskCommand(
                        rows.size(), errors.isEmpty() ? null : errorFileUrl, errors,
                        task.version(), 0L, "FILE_VALIDATE:" + task.importTaskNo()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyValidatedRows(String importTaskNo) {
        MdmImportQualityMapper.ImportTaskRow task = taskMapper.findImportTask(importTaskNo);
        applyValidatedRows(task, task == null ? -1 : task.version());
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyValidatedRows(String importTaskNo, long expectedVersion) {
        MdmImportQualityMapper.ImportTaskRow task = taskMapper.findImportTask(importTaskNo);
        applyValidatedRows(task, expectedVersion);
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimAndApplyValidatedRows(String importTaskNo, long expectedVersion) {
        if (taskMapper.claimImportTask(importTaskNo, expectedVersion) != 1) {
            throw new IllegalStateException("import task is already processing or version conflicted");
        }
        applyValidatedRows(importTaskNo, expectedVersion);
        taskMapper.releaseImportTask(importTaskNo);
    }

    private void applyValidatedRows(MdmImportQualityMapper.ImportTaskRow task, long expectedVersion) {
        if (task == null) {
            throw new IllegalArgumentException("import task not found");
        }
        if (task.version() != expectedVersion) {
            throw new IllegalStateException("import task version conflict");
        }
        for (MdmImportQualityMapper.ImportStagingRow row : taskMapper.listImportStaging(task.importTaskNo())) {
            MasterDataRecordMapper.RecordRow existing = recordMapper.findRecordByCode(task.typeCode(), row.dataCode());
            if (existing == null) {
                recordService.create(new MasterDataRecordApplicationService.CreateRecordCommand(
                        task.typeCode(), row.dataCode(), row.dataName(), row.dataPayload(), 0L,
                        task.importTaskNo() + ':' + row.rowNo()));
            } else if (!"CREATE".equalsIgnoreCase(task.importMode())) {
                recordService.change(existing.recordNo(), new MasterDataRecordApplicationService.ChangeRecordCommand(
                        row.dataName(), row.dataPayload(), "FILE_IMPORT:" + task.importTaskNo(),
                        existing.version(), 0L, task.importTaskNo() + ':' + row.rowNo()));
            }
        }
        importService.executeImportTask(task.importTaskNo(),
                new MdmImportQualityApplicationService.StateCommand(task.version(), 0L,
                        "FILE_EXECUTE:" + task.importTaskNo()));
    }

    private List<MdmImportQualityMapper.ImportErrorRow> validate(
            MdmImportQualityMapper.ImportTaskRow task, List<Map<String, String>> rows) {
        List<MdmImportQualityMapper.ImportErrorRow> errors = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        for (int index = 0; index < rows.size(); index++) {
            int rowNo = index + 2;
            Map<String, String> row = rows.get(index);
            String code = trim(row.get("dataCode"));
            String name = trim(row.get("dataName"));
            if (code == null) {
                errors.add(error(task, rowNo, "dataCode", "REQUIRED", "dataCode 必填", row));
            } else if (!codes.add(code)) {
                errors.add(error(task, rowNo, "dataCode", "DUPLICATE_IN_FILE", "文件内 dataCode 重复", row));
            }
            if (name == null) {
                errors.add(error(task, rowNo, "dataName", "REQUIRED", "dataName 必填", row));
            }
            if (code != null) {
                MasterDataRecordMapper.RecordRow existing = recordMapper.findRecordByCode(task.typeCode(), code);
                if ("CREATE".equalsIgnoreCase(task.importMode()) && existing != null
                        && "REJECT".equalsIgnoreCase(task.duplicatePolicy())) {
                    errors.add(error(task, rowNo, "dataCode", "ALREADY_EXISTS", "主数据编码已存在", row));
                }
                if ("UPDATE".equalsIgnoreCase(task.importMode()) && existing == null) {
                    errors.add(error(task, rowNo, "dataCode", "NOT_FOUND", "更新模式要求主数据已存在", row));
                }
            }
        }
        return errors;
    }

    private MdmImportQualityMapper.ImportErrorRow error(MdmImportQualityMapper.ImportTaskRow task,
                                                        int rowNo, String fieldCode, String errorCode,
                                                        String message, Map<String, String> row) {
        return new MdmImportQualityMapper.ImportErrorRow(null, task.importTaskNo(), rowNo, fieldCode,
                errorCode, message, toJson(row));
    }

    private String toJson(Map<String, String> row) {
        try {
            return objectMapper.writeValueAsString(row);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("导入行无法序列化", exception);
        }
    }

    private void verifyDigest(String expected, byte[] content) {
        if (expected == null || !expected.matches(SHA_256_PATTERN)) {
            return;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            String actual = java.util.HexFormat.of().formatHex(digest);
            if (!actual.equalsIgnoreCase(expected)) {
                throw new IllegalArgumentException("导入文件摘要不匹配");
            }
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public byte[] errorCsv(List<MdmImportQualityMapper.ImportErrorRow> errors) {
        StringBuilder csv = new StringBuilder("\uFEFFrowNo,fieldCode,errorCode,errorMessage,rawPayload\r\n");
        for (MdmImportQualityMapper.ImportErrorRow error : errors) {
            csv.append(error.rowNo()).append(',')
                    .append(escape(error.fieldCode())).append(',')
                    .append(escape(error.errorCode())).append(',')
                    .append(escape(error.errorMessage())).append(',')
                    .append(escape(error.rawPayload())).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
}
