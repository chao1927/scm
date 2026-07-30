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

/**
 * MdmImportQualityApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class MdmImportQualityApplicationService {

    /**
     * mapper（类型：{@code MdmImportQualityMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmImportQualityMapper mapper;

    /**
     * mdmMapper（类型：{@code MdmMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MdmMapper mdmMapper;

    /**
     * importSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong importSequence = new AtomicLong(500000);

    /**
     * exportSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong exportSequence = new AtomicLong(600000);

    /**
     * issueSequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong issueSequence = new AtomicLong(700000);

    /**
     * 创建 MdmImportQualityApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code MdmImportQualityMapper}
     * @param mdmMapper 持久化访问依赖，类型为 {@code MdmMapper}
     */
    public MdmImportQualityApplicationService(MdmImportQualityMapper mapper, MdmMapper mdmMapper) {
        this.mapper = mapper;
        this.mdmMapper = mdmMapper;
    }

    /**
     * 执行命令 {@code createImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateImportTaskCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.ImportTaskRow createImportTask(CreateImportTaskCommand command) {
        ensureType(command.typeCode());
        MdmImportQualityMapper.ImportTaskRow existing = mapper.findImportTaskByHash(command.typeCode(), command.fileHash(), command.importMode());
        if (existing != null) {
            return existing;
        }
        ImportTaskAggregate aggregate = ImportTaskAggregate.create("IMP" + importSequence.incrementAndGet(), command.typeCode(), command.fileName(), command.fileUrl(), command.fileHash(), command.importMode(), command.validateOnly(), command.duplicatePolicy());
        MdmImportQualityMapper.ImportTaskRow row = toRow(aggregate);
        mapper.insertImportTask(row);
        saveEvents(aggregate.pullEvents());
        log("CREATE_IMPORT_TASK", row.importTaskNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 校验业务约束 {@code validateImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code ValidateImportTaskCommand}
     * @return 校验业务约束的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.ImportTaskRow validateImportTask(String importTaskNo, ValidateImportTaskCommand command) {
        ImportTaskAggregate aggregate = loadImportTask(importTaskNo);
        aggregate.validateFile(command.totalCount(), command.errors().size(), command.errorFileUrl(), command.expectedVersion());
        mapper.updateImportTask(toRow(aggregate));
        for (MdmImportQualityMapper.ImportErrorRow error : command.errors()) {
            mapper.insertImportError(new MdmImportQualityMapper.ImportErrorRow(null, importTaskNo, error.rowNo(), error.fieldCode(), error.errorCode(), error.errorMessage(), error.rawPayload()));
        }
        saveEvents(aggregate.pullEvents());
        log("VALIDATE_IMPORT_TASK", importTaskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findImportTask(importTaskNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code executeImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 执行命令 {@code cancelImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code CancelCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.ImportTaskRow cancelImportTask(String importTaskNo, CancelCommand command) {
        ImportTaskAggregate aggregate = loadImportTask(importTaskNo);
        aggregate.cancel(command.reason(), command.expectedVersion());
        mapper.updateImportTask(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CANCEL_IMPORT_TASK", importTaskNo, command.operatorId(), command.idempotencyKey());
        return mapper.findImportTask(importTaskNo);
    }

    /**
     * 查询并返回 {@code listImportTasks}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<MdmImportQualityMapper.ImportTaskRow>}
     */
    public List<MdmImportQualityMapper.ImportTaskRow> listImportTasks(String typeCode, Integer status) {
        return mapper.listImportTasks(emptyToNull(typeCode), status);
    }

    /**
     * 查询并返回 {@code getImportTask}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    public MdmImportQualityMapper.ImportTaskRow getImportTask(String importTaskNo) {
        return mapper.findImportTask(importTaskNo);
    }

    /**
     * 查询并返回 {@code listImportErrors}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<MdmImportQualityMapper.ImportErrorRow>}
     */
    public List<MdmImportQualityMapper.ImportErrorRow> listImportErrors(String importTaskNo) {
        return mapper.listImportErrors(importTaskNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code template}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ImportTemplate}
     */
    public ImportTemplate template(String typeCode) {
        ensureType(typeCode);
        String payload = mdmMapper.listTemplates().stream().filter(row -> row.typeCode().equals(typeCode)).findFirst().map(MdmMapper.TemplateRow::fieldPayload).orElse("[]");
        return new ImportTemplate(typeCode, typeCode + "-import-template.csv", payload);
    }

    /**
     * 执行命令 {@code createExportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateExportTaskCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.ExportTaskRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.ExportTaskRow createExportTask(CreateExportTaskCommand command) {
        ensureType(command.typeCode());
        MdmImportQualityMapper.ExportTaskRow row = new MdmImportQualityMapper.ExportTaskRow(null, "EXP" + exportSequence.incrementAndGet(), command.typeCode(), command.filterPayload(), command.fieldPayload(), command.maskSensitiveFields(), 1, null, 1);
        mapper.insertExportTask(row);
        mapper.insertOutbox(new MdmMapper.OutboxRow("MasterDataExportTaskCreated", row.exportTaskNo(), row.typeCode(), 1, LocalDateTime.now()));
        log("CREATE_EXPORT_TASK", row.exportTaskNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 查询并返回 {@code listExportTasks}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmImportQualityMapper.ExportTaskRow>}
     */
    public List<MdmImportQualityMapper.ExportTaskRow> listExportTasks() {
        return mapper.listExportTasks();
    }

    /**
     * 处理当前类型职责中的操作 {@code raiseQualityIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code RaiseQualityIssueCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.QualityIssueRow raiseQualityIssue(RaiseQualityIssueCommand command) {
        DataQualityIssueAggregate aggregate = DataQualityIssueAggregate.raise("DQI" + issueSequence.incrementAndGet(), command.typeCode(), command.dataCode(), command.issueType(), command.issueDescription());
        MdmImportQualityMapper.QualityIssueRow row = toRow(aggregate);
        mapper.insertQualityIssue(row);
        saveEvents(aggregate.pullEvents());
        log("RAISE_QUALITY_ISSUE", row.issueNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    /**
     * 处理当前类型职责中的操作 {@code assignQualityIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code AssignIssueCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.QualityIssueRow assignQualityIssue(String issueNo, AssignIssueCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.assign(command.assigneeId(), command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("ASSIGN_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code fixQualityIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code FixIssueCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.QualityIssueRow fixQualityIssue(String issueNo, FixIssueCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.markFixed(command.resolution(), command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("FIX_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    /**
     * 处理当前类型职责中的操作 {@code verifyQualityIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.QualityIssueRow verifyQualityIssue(String issueNo, StateCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.verify(command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("VERIFY_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    /**
     * 执行命令 {@code closeQualityIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code StateCommand}
     * @return 执行命令的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    @Transactional(rollbackFor = Exception.class)
    public MdmImportQualityMapper.QualityIssueRow closeQualityIssue(String issueNo, StateCommand command) {
        DataQualityIssueAggregate aggregate = loadIssue(issueNo);
        aggregate.close(command.expectedVersion());
        mapper.updateQualityIssue(toRow(aggregate));
        saveEvents(aggregate.pullEvents());
        log("CLOSE_QUALITY_ISSUE", issueNo, command.operatorId(), command.idempotencyKey());
        return mapper.findQualityIssue(issueNo);
    }

    /**
     * 查询并返回 {@code listQualityIssues}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<MdmImportQualityMapper.QualityIssueRow>}
     */
    public List<MdmImportQualityMapper.QualityIssueRow> listQualityIssues(String typeCode, Integer status) {
        return mapper.listQualityIssues(emptyToNull(typeCode), status);
    }

    /**
     * 校验业务约束 {@code ensureType}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     */
    private void ensureType(String typeCode) {
        if (mdmMapper.findType(typeCode) == null) {
            throw new IllegalStateException("type does not exist");
        }
    }

    /**
     * 查询并返回 {@code loadImportTask}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ImportTaskAggregate}
     */
    private ImportTaskAggregate loadImportTask(String importTaskNo) {
        MdmImportQualityMapper.ImportTaskRow row = mapper.findImportTask(importTaskNo);
        if (row == null) {
            throw new IllegalArgumentException("import task not found");
        }
        return ImportTaskAggregate.restore(row.importTaskNo(), row.typeCode(), row.fileName(), row.fileUrl(), row.fileHash(), row.importMode(), row.validateOnly(), row.duplicatePolicy(), row.status(), row.totalCount(), row.successCount(), row.failedCount(), row.errorFileUrl(), row.reason(), row.version());
    }

    /**
     * 查询并返回 {@code loadIssue}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code DataQualityIssueAggregate}
     */
    private DataQualityIssueAggregate loadIssue(String issueNo) {
        MdmImportQualityMapper.QualityIssueRow row = mapper.findQualityIssue(issueNo);
        if (row == null) {
            throw new IllegalArgumentException("quality issue not found");
        }
        return DataQualityIssueAggregate.restore(row.issueNo(), row.typeCode(), row.dataCode(), row.issueType(), row.issueDescription(), row.status(), row.assigneeId(), row.resolution(), row.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code ImportTaskAggregate}
     * @return 转换数据模型的结果，类型为 {@code MdmImportQualityMapper.ImportTaskRow}
     */
    private MdmImportQualityMapper.ImportTaskRow toRow(ImportTaskAggregate aggregate) {
        return new MdmImportQualityMapper.ImportTaskRow(null, aggregate.importTaskNo(), aggregate.typeCode(), aggregate.fileName(), aggregate.fileUrl(), aggregate.fileHash(), aggregate.importMode(), aggregate.validateOnly(), aggregate.duplicatePolicy(), aggregate.status(), aggregate.totalCount(), aggregate.successCount(), aggregate.failedCount(), aggregate.errorFileUrl(), aggregate.reason(), aggregate.version());
    }

    /**
     * 转换数据模型 {@code toRow}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code DataQualityIssueAggregate}
     * @return 转换数据模型的结果，类型为 {@code MdmImportQualityMapper.QualityIssueRow}
     */
    private MdmImportQualityMapper.QualityIssueRow toRow(DataQualityIssueAggregate aggregate) {
        return new MdmImportQualityMapper.QualityIssueRow(null, aggregate.issueNo(), aggregate.typeCode(), aggregate.dataCode(), aggregate.issueType(), aggregate.issueDescription(), aggregate.status(), aggregate.assigneeId(), aggregate.resolution(), aggregate.version());
    }

    /**
     * 执行命令 {@code saveEvents}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param events 业务处理参数或成员，类型为 {@code List<MdmEvent>}
     */
    private void saveEvents(List<MdmEvent> events) {
        for (MdmEvent event : events) {
            mapper.insertOutbox(new MdmMapper.OutboxRow(event.eventType(), event.businessNo(), event.payload(), 1, event.occurredAt()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code log}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param operationType 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code Long}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     */
    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new MdmMapper.OperationLogRow(operationType, businessNo, operatorId, idempotencyKey, LocalDateTime.now()));
    }

    /**
     * 处理当前类型职责中的操作 {@code emptyToNull}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * CreateImportTaskCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateImportTaskCommand(String typeCode, String fileName, String fileUrl, String fileHash, String importMode, boolean validateOnly, String duplicatePolicy, Long operatorId, String idempotencyKey) {
    }

    /**
     * ValidateImportTaskCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ValidateImportTaskCommand(int totalCount, String errorFileUrl, List<MdmImportQualityMapper.ImportErrorRow> errors, long expectedVersion, Long operatorId, String idempotencyKey) {

        public ValidateImportTaskCommand {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
    }

    /**
     * StateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record StateCommand(long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * CancelCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CancelCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * ImportTemplate。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ImportTemplate(String typeCode, String fileName, String fieldPayload) {
    }

    /**
     * CreateExportTaskCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateExportTaskCommand(String typeCode, String filterPayload, String fieldPayload, boolean maskSensitiveFields, Long operatorId, String idempotencyKey) {
    }

    /**
     * RaiseQualityIssueCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record RaiseQualityIssueCommand(String typeCode, String dataCode, String issueType, String issueDescription, Long operatorId, String idempotencyKey) {
    }

    /**
     * AssignIssueCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AssignIssueCommand(Long assigneeId, long expectedVersion, Long operatorId, String idempotencyKey) {
    }

    /**
     * FixIssueCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record FixIssueCommand(String resolution, long expectedVersion, Long operatorId, String idempotencyKey) {
    }
}
