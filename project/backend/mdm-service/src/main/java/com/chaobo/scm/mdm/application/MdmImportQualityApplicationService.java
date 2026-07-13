package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.DataQualityIssueAggregate;
import com.chaobo.scm.mdm.domain.ImportTaskAggregate;
import com.chaobo.scm.mdm.domain.MdmEvent;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MdmImportQualityApplicationService {
    private final MdmImportQualityMapper mapper;
    private final MdmMapper mdmMapper;
    private final AtomicLong importSequence = new AtomicLong(500000);
    private final AtomicLong exportSequence = new AtomicLong(600000);
    private final AtomicLong issueSequence = new AtomicLong(700000);

    public MdmImportQualityApplicationService(MdmImportQualityMapper mapper, MdmMapper mdmMapper) {
        this.mapper = mapper;
        this.mdmMapper = mdmMapper;
    }

    @Transactional
    public MdmImportQualityMapper.ImportTaskRow createImportTask(CreateImportTaskCommand command) {
        ensureType(command.typeCode());
        MdmImportQualityMapper.ImportTaskRow existing = mapper.findImportTaskByHash(
                command.typeCode(), command.fileHash(), command.importMode());
        if (existing != null) {
            return existing;
        }
        ImportTaskAggregate aggregate = ImportTaskAggregate.create("IMP" + importSequence.incrementAndGet(),
                command.typeCode(), command.fileName(), command.fileUrl(), command.fileHash(), command.importMode(),
                command.validateOnly(), command.duplicatePolicy());
        MdmImportQualityMapper.ImportTaskRow row = toRow(aggregate);
        mapper.insertImportTask(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_IMPORT_TASK", row.importTaskNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmImportQualityMapper.ImportTaskRow validateImportTask(String importTaskNo, ValidateImportTaskCommand command) {
        ImportTaskAggregate aggregate = loadImportTask(importTaskNo);
        aggregate.validateFile(command.totalCount(), command.errors().size(), command.errorFileUrl(),
                command.expectedVersion());
        mapper.updateImportTask(toRow(aggregate));
        for (MdmImportQualityMapper.ImportErrorRow error : command.errors()) {
            mapper.insertImportError(new MdmImportQualityMapper.ImportErrorRow(null, importTaskNo, error.rowNo(),
                    error.fieldCode(), error.errorCode(), error.errorMessage(), error.rawPayload()));
        }
        saveEvents(aggregate.pullEvents());
        log("VALIDATE_IMPORT_TASK", importTaskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findImportTask(importTaskNo);
    }

    @Transactional
    public MdmImportQualityMapper.ImportTaskRow executeImportTask(String importTaskNo, StateCommand command) {
        ImportTaskAggregate aggregate = loadImportTask(importTaskNo);
        aggregate.execute(command.expectedVersion());
        mapper.updateImportTask(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        aggregate = loadImportTask(importTaskNo);
        aggregate.complete(aggregate.version());
        mapper.updateImportTask(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("EXECUTE_IMPORT_TASK", importTaskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findImportTask(importTaskNo);
    }

    @Transactional
    public MdmImportQualityMapper.ImportTaskRow cancelImportTask(String importTaskNo, CancelCommand command) {
        ImportTaskAggregate aggregate = loadImportTask(importTaskNo);
        aggregate.cancel(command.reason(), command.expectedVersion());
        mapper.updateImportTask(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CANCEL_IMPORT_TASK", importTaskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findImportTask(importTaskNo);
    }

    public List<MdmImportQualityMapper.ImportTaskRow> listImportTasks(String typeCode, Integer status) {
        return mapper.listImportTasks(emptyToNull(typeCode), status);
    }

    public MdmImportQualityMapper.ImportTaskRow getImportTask(String importTaskNo) {
        return mapper.findImportTask(importTaskNo);
    }

    public List<MdmImportQualityMapper.ImportErrorRow> listImportErrors(String importTaskNo) {
        return mapper.listImportErrors(importTaskNo);
    }

    public ImportTemplate template(String typeCode) {
        ensureType(typeCode);
        String payload = mdmMapper.listTemplates().stream()
                .filter(row -> row.typeCode().equals(typeCode))
                .findFirst()
                .map(MdmMapper.TemplateRow::fieldPayload)
                .orElse("[]");
        return new ImportTemplate(typeCode, typeCode + "-import-template.csv", payload);
    }

    @Transactional
    public MdmImportQualityMapper.ExportTaskRow createExportTask(CreateExportTaskCommand command) {
        ensureType(command.typeCode());
        MdmImportQualityMapper.ExportTaskRow row = new MdmImportQualityMapper.ExportTaskRow(null,
                "EXP" + exportSequence.incrementAndGet(), command.typeCode(), command.filterPayload(),
                command.fieldPayload(), command.maskSensitiveFields(), 1, null, 1);
        mapper.insertExportTask(row);
        mapper.insertOutbox(new MdmMapper.OutboxRow("MasterDataExportTaskCreated", row.exportTaskNo(),
                row.typeCode(), 1, LocalDateTime.now()));
        log("CREATE_EXPORT_TASK", row.exportTaskNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    public List<MdmImportQualityMapper.ExportTaskRow> listExportTasks() {
        return mapper.listExportTasks();
    }

    @Transactional
    public MdmImportQualityMapper.QualityIssueRow raiseQualityIssue(RaiseQualityIssueCommand command) {
        DataQualityIssueAggregate aggregate = DataQualityIssueAggregate.raise("DQI" + issueSequence.incrementAndGet(),
                command.typeCode(), command.dataCode(), command.issueType(), command.issueDescription());
        MdmImportQualityMapper.QualityIssueRow row = toRow(aggregate);
        mapper.insertQualityIssue(row);
        saveEvents(aggregate.pullEvents());
        log("RAISE_QUALITY_ISSUE", row.issueNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public MdmImportQualityMapper.QualityIssueRow assignQualityIssue(String issueNo, AssignIssueCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.assign(command.assigneeId(), command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("ASSIGN_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    @Transactional
    public MdmImportQualityMapper.QualityIssueRow fixQualityIssue(String issueNo, FixIssueCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.markFixed(command.resolution(), command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("FIX_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    @Transactional
    public MdmImportQualityMapper.QualityIssueRow verifyQualityIssue(String issueNo, StateCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.verify(command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("VERIFY_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    @Transactional
    public MdmImportQualityMapper.QualityIssueRow closeQualityIssue(String issueNo, StateCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.close(command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CLOSE_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    public List<MdmImportQualityMapper.QualityIssueRow> listQualityIssues(String typeCode, Integer status) {
        return mapper.listQualityIssues(emptyToNull(typeCode), status);
    }

    private void ensureType(String typeCode) {
        if (mdmMapper.findType(typeCode) == null) {
            throw new IllegalStateException("type does not exist");
        }
    }

    private ImportTaskAggregate loadImportTask(String importTaskNo) {
        MdmImportQualityMapper.ImportTaskRow row = mapper.findImportTask(importTaskNo);
        if (row == null) {
            throw new IllegalArgumentException("import task not found");
        }
        return ImportTaskAggregate.restore(row.importTaskNo(), row.typeCode(), row.fileName(), row.fileUrl(),
                row.fileHash(), row.importMode(), row.validateOnly(), row.duplicatePolicy(), row.status(),
                row.totalCount(), row.successCount(), row.failedCount(), row.errorFileUrl(), row.reason(),
                row.version());
    }

    private DataQualityIssueAggregate loadIssue(String issueNo) {
        MdmImportQualityMapper.QualityIssueRow row = mapper.findQualityIssue(issueNo);
        if (row == null) {
            throw new IllegalArgumentException("quality issue not found");
        }
        return DataQualityIssueAggregate.restore(row.issueNo(), row.typeCode(), row.dataCode(), row.issueType(),
                row.issueDescription(), row.status(), row.assigneeId(), row.resolution(), row.version());
    }

    private MdmImportQualityMapper.ImportTaskRow toRow(ImportTaskAggregate aggregate) {
        return new MdmImportQualityMapper.ImportTaskRow(null, aggregate.importTaskNo(), aggregate.typeCode(),
                aggregate.fileName(), aggregate.fileUrl(), aggregate.fileHash(), aggregate.importMode(),
                aggregate.validateOnly(), aggregate.duplicatePolicy(), aggregate.status(), aggregate.totalCount(),
                aggregate.successCount(), aggregate.failedCount(), aggregate.errorFileUrl(), aggregate.reason(),
                aggregate.version());
    }

    private MdmImportQualityMapper.QualityIssueRow toRow(DataQualityIssueAggregate aggregate) {
        return new MdmImportQualityMapper.QualityIssueRow(null, aggregate.issueNo(), aggregate.typeCode(),
                aggregate.dataCode(), aggregate.issueType(), aggregate.issueDescription(), aggregate.status(),
                aggregate.assigneeId(), aggregate.resolution(), aggregate.version());
    }

    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1,
                    event.occurredAt()));
        }
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey, LocalDateTime.now()));
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public record CreateImportTaskCommand(String typeCode, String fileName, String fileUrl, String fileHash,
                                          String importMode, boolean validateOnly, String duplicatePolicy,
                                          Long operatorId, String idempotencyKey) {}

    public record ValidateImportTaskCommand(int totalCount, String errorFileUrl,
                                            List<MdmImportQualityMapper.ImportErrorRow> errors,
                                            long expectedVersion, Long operatorId, String idempotencyKey) {
        public ValidateImportTaskCommand {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
    }

    public record StateCommand(long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record CancelCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record ImportTemplate(String typeCode, String fileName, String fieldPayload) {}
    public record CreateExportTaskCommand(String typeCode, String filterPayload, String fieldPayload,
                                          boolean maskSensitiveFields, Long operatorId, String idempotencyKey) {}
    public record RaiseQualityIssueCommand(String typeCode, String dataCode, String issueType,
                                           String issueDescription, Long operatorId, String idempotencyKey) {}
    public record AssignIssueCommand(Long assigneeId, long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record FixIssueCommand(String resolution, long expectedVersion, Long operatorId, String idempotencyKey) {}
}
