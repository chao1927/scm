package com.chaobo.scm.mdm.application;

import com.chaobo.scm.mdm.domain.ImportTaskAggregate;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmImportQualityMapper;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmMapper;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MdmImportQualityApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmImportQualityApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code importTaskIsIdempotentAndKeepsErrorsAndEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void importTaskIsIdempotentAndKeepsErrorsAndEvents() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mdmMapper = new MasterDataRecordApplicationServiceTest.MemoryMdmMapper();
        mdmMapper.types.put("SKU", new MdmMapper.TypeRow(null, "SKU", "商品SKU", "PRODUCT", 2, 2));
        MemoryImportQualityMapper mapper = new MemoryImportQualityMapper();
        MdmImportQualityApplicationService service = new MdmImportQualityApplicationService(mapper, mdmMapper);
        MdmImportQualityMapper.ImportTaskRow task = service.createImportTask(new MdmImportQualityApplicationService.CreateImportTaskCommand("SKU", "sku.csv", "oss://sku.csv", "hash-1", "CREATE", false, "REJECT", 1001L, "idem-1"));
        MdmImportQualityMapper.ImportTaskRow duplicate = service.createImportTask(new MdmImportQualityApplicationService.CreateImportTaskCommand("SKU", "sku-copy.csv", "oss://sku-copy.csv", "hash-1", "CREATE", false, "REJECT", 1001L, "idem-2"));
        service.validateImportTask(task.importTaskNo(), new MdmImportQualityApplicationService.ValidateImportTaskCommand(2, "oss://sku-error.csv", List.of(new MdmImportQualityMapper.ImportErrorRow(null, task.importTaskNo(), 2, "taxRate", "REQUIRED", "税率必填", "{}")), task.version(), 1001L, "idem-3"));
        MdmImportQualityMapper.ImportTaskRow executed = service.executeImportTask(task.importTaskNo(), new MdmImportQualityApplicationService.StateCommand(2, 1001L, "idem-4"));
        assertThat(duplicate.importTaskNo()).isEqualTo(task.importTaskNo());
        assertThat(executed.status()).isEqualTo(ImportTaskAggregate.PARTIAL_FAILED);
        assertThat(service.listImportErrors(task.importTaskNo())).hasSize(1);
        assertThat(mapper.outbox).extracting(MdmMapper.OutboxRow::eventType).contains("ImportTaskCreated", "ImportFileValidated", "ImportTaskExecuted", "ImportTaskCompleted");
    }

    /**
     * 处理当前类型职责中的操作 {@code qualityIssueCanMoveThroughGovernanceLifecycle}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void qualityIssueCanMoveThroughGovernanceLifecycle() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mdmMapper = new MasterDataRecordApplicationServiceTest.MemoryMdmMapper();
        MemoryImportQualityMapper mapper = new MemoryImportQualityMapper();
        MdmImportQualityApplicationService service = new MdmImportQualityApplicationService(mapper, mdmMapper);
        MdmImportQualityMapper.QualityIssueRow issue = service.raiseQualityIssue(new MdmImportQualityApplicationService.RaiseQualityIssueCommand("SKU", "SKU-001", "MISSING_FIELD", "缺少税率", 1001L, "idem-1"));
        issue = service.assignQualityIssue(issue.issueNo(), new MdmImportQualityApplicationService.AssignIssueCommand(1002L, issue.version(), 1001L, "idem-2"));
        issue = service.fixQualityIssue(issue.issueNo(), new MdmImportQualityApplicationService.FixIssueCommand("已修复", issue.version(), 1002L, "idem-3"));
        issue = service.verifyQualityIssue(issue.issueNo(), new MdmImportQualityApplicationService.StateCommand(issue.version(), 1003L, "idem-4"));
        issue = service.closeQualityIssue(issue.issueNo(), new MdmImportQualityApplicationService.StateCommand(issue.version(), 1003L, "idem-5"));
        assertThat(issue.status()).isEqualTo(5);
        assertThat(mapper.logs).extracting(MdmMapper.OperationLogRow::operationType).contains("RAISE_QUALITY_ISSUE", "CLOSE_QUALITY_ISSUE");
    }

    @Test
    void exportTaskPersistsServerAuthorizedFieldsAndForcedMaskSnapshot() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mdmMapper =
            mdmMapperWithTemplate();
        MemoryImportQualityMapper mapper = new MemoryImportQualityMapper();
        MdmImportQualityApplicationService service =
            new MdmImportQualityApplicationService(mapper, mdmMapper);

        MdmImportQualityMapper.ExportTaskRow safeDefault = service.createExportTask(
            new MdmImportQualityApplicationService.CreateExportTaskCommand(
                "SKU", "{\"status\":2}", null, false, 9999L, "export-1"),
            access(Set.of("master-data:importexport:export")));

        assertThat(safeDefault.fieldPayload())
            .isEqualTo("[\"dataCode\",\"dataName\",\"status\"]");
        assertThat(safeDefault.maskSensitiveFields()).isTrue();
        assertThat(safeDefault.filterPayload()).isEqualTo("{\"status\":2}");

        MdmImportQualityMapper.ExportTaskRow privileged = service.createExportTask(
            new MdmImportQualityApplicationService.CreateExportTaskCommand(
                "SKU", "{\"dataCodePrefix\":\"SKU-\"}",
                "[\"dataCode\",\"mobile\"]", false, 9999L, "export-2"),
            access(Set.of("master-data:importexport:export",
                "mdm:export:field:mobile", "mdm:export:sensitive:unmask")));

        assertThat(privileged.fieldPayload())
            .isEqualTo("[\"dataCode\",\"mobile\"]");
        assertThat(privileged.maskSensitiveFields()).isFalse();
        assertThat(mapper.logs.get(mapper.logs.size() - 1).operatorId()).isEqualTo(1001L);
    }

    @Test
    void exportTaskRejectsUnknownFiltersAndUnauthorizedOrUnsupportedFields() {
        MdmImportQualityApplicationService service =
            new MdmImportQualityApplicationService(
                new MemoryImportQualityMapper(), mdmMapperWithTemplate());
        ScmAccessContext ordinary = access(Set.of("master-data:importexport:export"));

        assertThatThrownBy(() -> service.createExportTask(
            new MdmImportQualityApplicationService.CreateExportTaskCommand(
                "SKU", "{\"unknown\":1}", null, true, 1L, "bad-filter"), ordinary))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unsupported export filter");
        assertThatThrownBy(() -> service.createExportTask(
            new MdmImportQualityApplicationService.CreateExportTaskCommand(
                "SKU", "{}", "[\"mobile\"]", true, 1L, "no-field"), ordinary))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("mobile");
        assertThatThrownBy(() -> service.createExportTask(
            new MdmImportQualityApplicationService.CreateExportTaskCommand(
                "SKU", "{}", "[\"notInTemplate\"]", true, 1L, "bad-field"),
            access(Set.of("mdm:*"))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unsupported export field");
    }

    private static MasterDataRecordApplicationServiceTest.MemoryMdmMapper
            mdmMapperWithTemplate() {
        MasterDataRecordApplicationServiceTest.MemoryMdmMapper mapper =
            new MasterDataRecordApplicationServiceTest.MemoryMdmMapper() {
                @Override
                public List<MdmMapper.TemplateRow> listTemplates() {
                    return List.of(new MdmMapper.TemplateRow(
                        1L, "SKU-TEMPLATE", "SKU",
                        "mobile:手机号:STRING:false:false:false;brand:品牌:STRING:false:false:false",
                        2, 1));
                }
            };
        mapper.types.put("SKU", new MdmMapper.TypeRow(
            null, "SKU", "商品SKU", "PRODUCT", 2, 2));
        return mapper;
    }

    private static ScmAccessContext access(Set<String> permissions) {
        return new ScmAccessContext(1001L, "mdm-operator", "MDM", permissions,
            Map.of());
    }

    /**
     * MemoryImportQualityMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryImportQualityMapper implements MdmImportQualityMapper {

        /**
         * importTasks（类型：{@code Map<String,ImportTaskRow>}）。
         *
         * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
         */
        final Map<String, ImportTaskRow> importTasks = new LinkedHashMap<>();

        /**
         * errors（类型：{@code List<ImportErrorRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<ImportErrorRow> errors = new ArrayList<>();

        /**
         * exports（类型：{@code List<ExportTaskRow>}）。
         *
         * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
         */
        final List<ExportTaskRow> exports = new ArrayList<>();

        /**
         * issues（类型：{@code Map<String,QualityIssueRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, QualityIssueRow> issues = new LinkedHashMap<>();

        /**
         * outbox（类型：{@code List<MdmMapper.OutboxRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<MdmMapper.OutboxRow> outbox = new ArrayList<>();

        /**
         * logs（类型：{@code List<MdmMapper.OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<MdmMapper.OperationLogRow> logs = new ArrayList<>();

        /**
         * 查询并返回 {@code findImportTask}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param importTaskNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ImportTaskRow}
         */
        @Override
        public ImportTaskRow findImportTask(String importTaskNo) {
            return importTasks.get(importTaskNo);
        }

        /**
         * 查询并返回 {@code findImportTaskByHash}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @param fileHash 业务处理参数或成员，类型为 {@code String}
         * @param importMode 应用或外部协作依赖，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ImportTaskRow}
         */
        @Override
        public ImportTaskRow findImportTaskByHash(String typeCode, String fileHash, String importMode) {
            return importTasks.values().stream().filter(row -> row.typeCode().equals(typeCode) && row.fileHash().equals(fileHash) && row.importMode().equals(importMode)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listImportTasks}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code Integer}
         * @return 查询并返回的结果，类型为 {@code List<ImportTaskRow>}
         */
        @Override
        public List<ImportTaskRow> listImportTasks(String typeCode, Integer status) {
            return importTasks.values().stream().filter(row -> typeCode == null || row.typeCode().equals(typeCode)).filter(row -> status == null || row.status() == status).toList();
        }

        @Override
        public List<ImportTaskRow> listPendingImportTasks(int limit, int maxRetries) {
            return importTasks.values().stream().filter(row -> row.status() == ImportTaskAggregate.PENDING)
                    .limit(limit).toList();
        }

        @Override
        public int claimImportTask(String importTaskNo, long version) {
            return importTasks.containsKey(importTaskNo) ? 1 : 0;
        }

        @Override
        public void failImportProcessing(String importTaskNo, String reason, int retryDelaySeconds) {
        }

        @Override
        public void releaseImportTask(String importTaskNo) {
        }

        /**
         * 处理当前类型职责中的操作 {@code insertImportTask}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ImportTaskRow}
         */
        @Override
        public void insertImportTask(ImportTaskRow row) {
            importTasks.put(row.importTaskNo(), row);
        }

        /**
         * 执行命令 {@code updateImportTask}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ImportTaskRow}
         */
        @Override
        public int updateImportTask(ImportTaskRow row) {
            importTasks.put(row.importTaskNo(), row);
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertImportError}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ImportErrorRow}
         */
        @Override
        public void insertImportError(ImportErrorRow row) {
            errors.add(row);
        }

        /**
         * 查询并返回 {@code listImportErrors}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param importTaskNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<ImportErrorRow>}
         */
        @Override
        public List<ImportErrorRow> listImportErrors(String importTaskNo) {
            return errors.stream().filter(row -> row.importTaskNo().equals(importTaskNo)).toList();
        }

        @Override
        public void insertImportStaging(ImportStagingRow row) {
        }

        @Override
        public List<ImportStagingRow> listImportStaging(String importTaskNo) {
            return List.of();
        }

        @Override
        public void deleteImportStaging(String importTaskNo) {
        }

        @Override
        public void deleteImportErrors(String importTaskNo) {
            errors.removeIf(row -> row.importTaskNo().equals(importTaskNo));
        }

        /**
         * 处理当前类型职责中的操作 {@code insertExportTask}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ExportTaskRow}
         */
        @Override
        public void insertExportTask(ExportTaskRow row) {
            exports.add(row);
        }

        /**
         * 查询并返回 {@code listExportTasks}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<ExportTaskRow>}
         */
        @Override
        public List<ExportTaskRow> listExportTasks() {
            return exports;
        }

        @Override
        public List<ExportTaskRow> listPendingExportTasks(int limit, int maxRetries) {
            return exports.stream().filter(row -> row.status() == 1).limit(limit).toList();
        }

        @Override
        public int claimExportTask(String exportTaskNo, long version) {
            return 1;
        }

        @Override
        public void completeExportTask(String exportTaskNo, String fileUrl) {
        }

        @Override
        public void failExportTask(String exportTaskNo, String reason, int retryDelaySeconds) {
        }

        @Override
        public ExportTaskRow findExportTask(String exportTaskNo) {
            return exports.stream().filter(row -> row.exportTaskNo().equals(exportTaskNo)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code findQualityIssue}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param issueNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code QualityIssueRow}
         */
        @Override
        public QualityIssueRow findQualityIssue(String issueNo) {
            return issues.get(issueNo);
        }

        /**
         * 查询并返回 {@code listQualityIssues}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param typeCode 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code Integer}
         * @return 查询并返回的结果，类型为 {@code List<QualityIssueRow>}
         */
        @Override
        public List<QualityIssueRow> listQualityIssues(String typeCode, Integer status) {
            return issues.values().stream().filter(row -> typeCode == null || row.typeCode().equals(typeCode)).filter(row -> status == null || row.status() == status).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertQualityIssue}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code QualityIssueRow}
         */
        @Override
        public void insertQualityIssue(QualityIssueRow row) {
            issues.put(row.issueNo(), row);
        }

        /**
         * 执行命令 {@code updateQualityIssue}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code QualityIssueRow}
         */
        @Override
        public void updateQualityIssue(QualityIssueRow row) {
            issues.put(row.issueNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutbox}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OutboxRow}
         */
        @Override
        public void insertOutbox(MdmMapper.OutboxRow row) {
            outbox.add(row);
        }

        /**
         * 查询并返回 {@code listOutbox}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OutboxRow>}
         */
        @Override
        public List<MdmMapper.OutboxRow> listOutbox() {
            return outbox;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OperationLogRow}
         */
        @Override
        public void insertOperationLog(MdmMapper.OperationLogRow row) {
            logs.add(row);
        }

        /**
         * 查询并返回 {@code listOperationLogs}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OperationLogRow>}
         */
        @Override
        public List<MdmMapper.OperationLogRow> listOperationLogs() {
            return logs;
        }
    }
}
