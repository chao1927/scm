package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.ImportTaskAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MdmImportQualityApplicationServiceTest {
    @Test
    void importTaskIsIdempotentAndKeepsErrorsAndEvents() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mdmMapper =
                new MasterDataRecordApplicationServiceTest.MemoryMdmMapper();
        mdmMapper.types.put("SKU", new MdmMapper.TypeRow(null, "SKU", "商品SKU", "PRODUCT", 2, 2));
        MemoryImportQualityMapper mapper = new MemoryImportQualityMapper();
        MdmImportQualityApplicationService service = new MdmImportQualityApplicationService(mapper, mdmMapper);

        MdmImportQualityMapper.ImportTaskRow task = service.createImportTask(new MdmImportQualityApplicationService.CreateImportTaskCommand(
                "SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT", 1001L, "idem-1"));
        MdmImportQualityMapper.ImportTaskRow duplicate = service.createImportTask(new MdmImportQualityApplicationService.CreateImportTaskCommand(
                "SKU", "sku-copy.csv", "oss://sku-copy.csv", "hash-1", "CREATE", false, "REJECT", 1001L, "idem-2"));
        service.validateImportTask(task.importTaskNo(), new MdmImportQualityApplicationService.ValidateImportTaskCommand(
                2, "oss://sku-error.csv", List.of(new MdmImportQualityMapper.ImportErrorRow(
                null, task.importTaskNo(), 2, "taxRate", "REQUIRED", "税率必填", "{}")), task.version(), 1001L, "idem-3"));
        MdmImportQualityMapper.ImportTaskRow executed = service.executeImportTask(task.importTaskNo(),
                new MdmImportQualityApplicationService.StateCommand(2, 1001L, "idem-4"));

        assertThat(duplicate.importTaskNo()).isEqualTo(task.importTaskNo());
        assertThat(executed.status()).isEqualTo(ImportTaskAggregate.PARTIAL_FAILED);
        assertThat(service.listImportErrors(task.importTaskNo())).hasSize(1);
        assertThat(mapper.outbox).extracting(MdmMapper.OutboxRow::eventType)
                .contains("ImportTaskCreated", "ImportFileValidated", "ImportTaskExecuted", "ImportTaskCompleted");
    }

    @Test
    void qualityIssueCanMoveThroughGovernanceLifecycle() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mdmMapper =
                new MasterDataRecordApplicationServiceTest.MemoryMdmMapper();
        MemoryImportQualityMapper mapper = new MemoryImportQualityMapper();
        MdmImportQualityApplicationService service = new MdmImportQualityApplicationService(mapper, mdmMapper);

        MdmImportQualityMapper.QualityIssueRow issue = service.raiseQualityIssue(
                new MdmImportQualityApplicationService.RaiseQualityIssueCommand(
                        "SKU", "SKU-001", "MISSING_FIELD", "缺少税率", 1001L, "idem-1"));
        issue = service.assignQualityIssue(issue.issueNo(),
                new MdmImportQualityApplicationService.AssignIssueCommand(1002L, issue.version(), 1001L, "idem-2"));
        issue = service.fixQualityIssue(issue.issueNo(),
                new MdmImportQualityApplicationService.FixIssueCommand("已修复", issue.version(), 1002L, "idem-3"));
        issue = service.verifyQualityIssue(issue.issueNo(),
                new MdmImportQualityApplicationService.StateCommand(issue.version(), 1003L, "idem-4"));
        issue = service.closeQualityIssue(issue.issueNo(),
                new MdmImportQualityApplicationService.StateCommand(issue.version(), 1003L, "idem-5"));

        assertThat(issue.status()).isEqualTo(5);
        assertThat(mapper.logs).extracting(MdmMapper.OperationLogRow::operationType)
                .contains("RAISE_QUALITY_ISSUE", "CLOSE_QUALITY_ISSUE");
    }

    static class MemoryImportQualityMapper implements MdmImportQualityMapper {
        final Map<String, ImportTaskRow> importTasks = new LinkedHashMap<>();
        final List<ImportErrorRow> errors = new ArrayList<>();
        final List<ExportTaskRow> exports = new ArrayList<>();
        final Map<String, QualityIssueRow> issues = new LinkedHashMap<>();
        final List<MdmMapper.OutboxRow> outbox = new ArrayList<>();
        final List<MdmMapper.OperationLogRow> logs = new ArrayList<>();

        @Override
        public ImportTaskRow findImportTask(String importTaskNo) { return importTasks.get(importTaskNo); }

        @Override
        public ImportTaskRow findImportTaskByHash(String typeCode, String fileHash, String importMode) {
            return importTasks.values().stream()
                    .filter(row -> row.typeCode().equals(typeCode) && row.fileHash().equals(fileHash)
                            && row.importMode().equals(importMode))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<ImportTaskRow> listImportTasks(String typeCode, Integer status) {
            return importTasks.values().stream()
                    .filter(row -> typeCode == null || row.typeCode().equals(typeCode))
                    .filter(row -> status == null || row.status() == status)
                    .toList();
        }

        @Override
        public void insertImportTask(ImportTaskRow row) { importTasks.put(row.importTaskNo(), row); }

        @Override
        public void updateImportTask(ImportTaskRow row) { importTasks.put(row.importTaskNo(), row); }

        @Override
        public void insertImportError(ImportErrorRow row) { errors.add(row); }

        @Override
        public List<ImportErrorRow> listImportErrors(String importTaskNo) {
            return errors.stream().filter(row -> row.importTaskNo().equals(importTaskNo)).toList();
        }

        @Override
        public void insertExportTask(ExportTaskRow row) { exports.add(row); }

        @Override
        public List<ExportTaskRow> listExportTasks() { return exports; }

        @Override
        public QualityIssueRow findQualityIssue(String issueNo) { return issues.get(issueNo); }

        @Override
        public List<QualityIssueRow> listQualityIssues(String typeCode, Integer status) {
            return issues.values().stream()
                    .filter(row -> typeCode == null || row.typeCode().equals(typeCode))
                    .filter(row -> status == null || row.status() == status)
                    .toList();
        }

        @Override
        public void insertQualityIssue(QualityIssueRow row) { issues.put(row.issueNo(), row); }

        @Override
        public void updateQualityIssue(QualityIssueRow row) { issues.put(row.issueNo(), row); }

        @Override
        public void insertOutbox(MdmMapper.OutboxRow row) { outbox.add(row); }

        @Override
        public List<MdmMapper.OutboxRow> listOutbox() { return outbox; }

        @Override
        public void insertOperationLog(MdmMapper.OperationLogRow row) { logs.add(row); }

        @Override
        public List<MdmMapper.OperationLogRow> listOperationLogs() { return logs; }
    }
}
