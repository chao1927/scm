package com.chaobo.scm.supplier.application.operations;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.application.operations.export.SupplierExportDefinitions;
import com.chaobo.scm.supplier.application.operations.export.SupplierExportObjectStoragePort;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.operations.SupplierOperationsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * SupplierOperationsApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierOperationsApplicationService {

    /**
     * mapper（类型：{@code SupplierOperationsMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierOperationsMapper mapper;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * 导出文件存储端口，只通过对象键暴露文件能力。
     */
    private final SupplierExportObjectStoragePort exportStorage;

    /**
     * 创建 SupplierOperationsApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierOperationsMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     */
    public SupplierOperationsApplicationService(SupplierOperationsMapper mapper, IdentifierGenerator ids,
                                                AuditLogRepository audit,
                                                SupplierExportObjectStoragePort exportStorage) {
        this.mapper = mapper;
        this.ids = ids;
        this.audit = audit;
        this.exportStorage = exportStorage;
    }

    /**
     * 处理当前类型职责中的操作 {@code accept}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param e 业务处理参数或成员，类型为 {@code OperationsEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void accept(OperationsEvent e) {
        switch(e.eventType()) {
            case "RfqPublished" ->
                work(e, "QUOTE", "RFQ", 2);
            case "SupplierContractSubmitted" ->
                work(e, "CONTRACT_APPROVAL", "CONTRACT", 1);
            case "PurchaseOrderReleased" ->
                work(e, "PO_CONFIRM", "PURCHASE_ORDER", 2);
            case "SupplierRectificationRequested" ->
                work(e, "RECTIFICATION", "QUALITY_ISSUE", 2);
            case "BmsReconciliationIssued" ->
                work(e, "RECONCILIATION", "RECONCILIATION", 2);
            case "SupplierReturnConfirmationRequested" ->
                work(e, "RETURN_CONFIRM", "SUPPLIER_RETURN", 2);
            case "SupplierQualificationExpiring" ->
                warning(e, "QUALIFICATION_EXPIRING", "QUALIFICATION", 2);
            case "SupplierContractExpiring" ->
                warning(e, "CONTRACT_EXPIRING", "CONTRACT", 2);
            case "SupplierQuoteExpiring" ->
                warning(e, "QUOTE_EXPIRING", "QUOTE", 1);
            case "AsnDelayed", "TmsShipmentDelayed", "SupplierRectificationOverdue" ->
                warning(e, e.eventType().toUpperCase(), e.businessType(), 3);
            case "SupplierScorePublished" ->
                warning(e, "LOW_SCORE", "SCORE_RESULT", 2);
            default ->
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "不支持的运营事件: " + e.eventType());
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code workItems}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.WorkItem>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<OperationViews.WorkItem> workItems(Long supplierId, Long scope, Integer status, int page, int size) {
        check(page, size);
        return mapper.workItems(scope == null ? supplierId : scope, status, (page - 1) * size, size);
    }

    /**
     * 处理当前类型职责中的操作 {@code warnings}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.Warning>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<OperationViews.Warning> warnings(Long supplierId, Long scope, Integer status, int page, int size) {
        check(page, size);
        return mapper.warnings(scope == null ? supplierId : scope, status, (page - 1) * size, size);
    }

    /**
     * 处理当前类型职责中的操作 {@code processWork}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param close 业务处理参数或成员，类型为 {@code boolean}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void processWork(long id, int version, boolean close, CommandContext c) {
        c.requirePermission("supplier:work-item:process");
        if (mapper.processWork(id, version, close ? PROCESS_WORK_VALUE_4 : PROCESS_WARNING_VALUE_3) != 1) {
            throw conflict();
        }
        audit.save(c, "PROCESS_WORK_ITEM", "WORK_ITEM", id, String.valueOf(id), null, "{\"status\":" + (close ? 4 : 3) + "}");
    }

    /**
     * 处理当前类型职责中的操作 {@code processWarning}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param close 业务处理参数或成员，类型为 {@code boolean}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void processWarning(long id, int version, boolean close, CommandContext c) {
        c.requirePermission("supplier:warning:process");
        if (mapper.processWarning(id, version, close ? PROCESS_WARNING_VALUE_3 : PROCESS_WARNING_VALUE_2) != 1) {
            throw conflict();
        }
        audit.save(c, "PROCESS_WARNING", "WARNING", id, String.valueOf(id), null, "{\"status\":" + (close ? 3 : 2) + "}");
    }

    /**
     * 处理当前类型职责中的操作 {@code failedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.FailedEvent>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<OperationViews.FailedEvent> failedEvents() {
        var all = new ArrayList<OperationViews.FailedEvent>();
        all.addAll(mapper.failedInbound(100));
        all.addAll(mapper.failedOutbound(100));
        all.sort(Comparator.comparing(OperationViews.FailedEvent::updatedAt).reversed());
        return all;
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param direction 业务处理参数或成员，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void replay(long id, String direction, String reason, CommandContext c) {
        c.requirePermission("supplier:event:replay");
        if (reason == null || reason.isBlank()) {
            throw rule("人工重放必须说明原因");
        }
        int changed = "OUTBOUND".equals(direction) ? mapper.replayOutbound(id, reason) : mapper.replayInbound(id, reason);
        if (changed != 1) {
            throw conflict();
        }
        audit.save(c, "REPLAY_" + direction + "_EVENT", "EVENT", id, String.valueOf(id), null, "{\"reason\":\"" + reason.replace("\"", "") + "\"}");
    }

    /**
     * 处理当前类型职责中的操作 {@code reconcile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param target 业务处理参数或成员，类型为 {@code String}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @param remoteCount 数量值，类型为 {@code long}
     * @param remoteAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void reconcile(String type, String target, LocalDate date, long remoteCount, BigDecimal remoteAmount, CommandContext c) {
        c.requirePermission("supplier:data-reconciliation:execute");
        long localCount;
        BigDecimal localAmount = null;
        switch(type) {
            case "ASN" ->
                localCount = mapper.localAsnCount(date);
            case "SUPPLIER_RETURN" ->
                localCount = mapper.localReturnCount(date);
            case "STATEMENT" ->
                {
                    localCount = mapper.localStatementCount(date);
                    localAmount = mapper.localStatementAmount(date);
                }
            default ->
                throw rule("不支持的对账类型");
        }
        boolean amountSame = localAmount == null || remoteAmount != null && localAmount.compareTo(remoteAmount) == 0, same = localCount == remoteCount && amountSame;
        String detail = same ? null : "本地数量=" + localCount + "，对方数量=" + remoteCount + (localAmount == null ? "" : "，本地金额=" + localAmount + "，对方金额=" + remoteAmount);
        mapper.upsertReconciliation(ids.nextId(), type, target, date, localCount, remoteCount, localAmount, remoteAmount, detail, same ? 1 : 2, c.operatorId());
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciliations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.Reconciliation>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<OperationViews.Reconciliation> reconciliations() {
        return mapper.reconciliations();
    }

    /**
     * 处理当前类型职责中的操作 {@code dashboard}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OperationViews.Dashboard}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public OperationViews.Dashboard dashboard() {
        return mapper.dashboard();
    }

    /**
     * 执行命令 {@code createExport}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param queryJson 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public long createExport(String type, Long supplierId, String queryJson, CommandContext c) {
        c.requirePermission("supplier:export:create");
        if (!SupplierExportDefinitions.supports(type)) {
            throw rule("不支持的导出类型");
        }
        if (c.supplierScopeId() != null && supplierId != null && !c.supplierScopeId().equals(supplierId)) {
            throw new BusinessException(ErrorCode.SUPPLIER_SCOPE_DENIED, "无权导出该供应商数据");
        }
        if (c.supplierScopeId() != null && Set.of(FAILED_EVENT, RECONCILIATION).contains(type)) {
            throw new BusinessException(ErrorCode.SUPPLIER_SCOPE_DENIED, "供应商账号无权导出全局运营数据");
        }
        Long effectiveSupplierId = c.supplierScopeId() == null ? supplierId : c.supplierScopeId();
        String effectiveQuery = queryJson == null || queryJson.isBlank() ? "{}" : queryJson;
        long id = ids.nextId();
        mapper.insertExport(id, type, effectiveSupplierId, effectiveQuery, c.operatorId(), c.idempotencyKey());
        var persisted = mapper.exportTaskByIdempotency(c.operatorId(), c.idempotencyKey());
        if (persisted == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出任务创建结果不可用");
        }
        if (!Objects.equals(type, persisted.exportType())
                || !Objects.equals(effectiveSupplierId, persisted.supplierId())
                || !Objects.equals(effectiveQuery, persisted.queryJson())) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "幂等键已被不同导出请求使用");
        }
        if (persisted.id() == id) {
            audit.save(c, "CREATE_EXPORT_TASK", "EXPORT_TASK", id, Long.toString(id), null,
                    "{\"type\":\"" + type + "\"}");
        }
        return persisted.id();
    }

    /**
     * 处理当前类型职责中的操作 {@code exportTasks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.ExportTask>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<OperationViews.ExportTask> exportTasks(Long supplierId, Long scope, Integer status, int page, int size) {
        check(page, size);
        return mapper.exportTasks(scope == null ? supplierId : scope, status, (page - 1) * size, size);
    }

    /**
     * 处理当前类型职责中的操作 {@code exportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OperationViews.ExportTask}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public OperationViews.ExportTask exportTask(long id, Long supplierScopeId) {
        var task = mapper.exportTask(id);
        boolean missing = task == null;
        boolean outsideScope = !missing && supplierScopeId != null && !supplierScopeId.equals(task.supplierId());
        if (missing || outsideScope) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在");
        }
        return task;
    }

    /**
     * 将失败导出任务重新放回待处理队列；失败原因和重试次数保留用于审计。
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryExport(long id, int version, CommandContext c) {
        c.requirePermission("supplier:export:retry");
        if (mapper.retryExport(id, version, c.supplierScopeId()) != 1) {
            throw conflict();
        }
        audit.save(c, "RETRY_EXPORT_TASK", "EXPORT_TASK", id, Long.toString(id), null,
                "{\"status\":1,\"version\":" + (version + 1) + "}");
    }

    /**
     * 在完成状态且数据范围允许时读取真实导出文件。
     */
    public ExportFile downloadExport(long id, Long supplierScopeId) {
        var task = exportTask(id, supplierScopeId);
        if (task.status() != EXPORT_COMPLETED_STATUS || task.objectKey() == null || task.objectKey().isBlank()) {
            throw rule("导出文件尚未生成");
        }
        var content = exportStorage.load(task.objectKey());
        return new ExportFile(task.fileName(), content.contentType(), content.bytes());
    }

    /**
     * 下载文件传输对象。
     */
    public record ExportFile(String fileName, String contentType, byte[] bytes) {
    }

    /**
     * 处理当前类型职责中的操作 {@code work}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param e 业务处理参数或成员，类型为 {@code OperationsEvent}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param business 业务处理参数或成员，类型为 {@code String}
     * @param assignee 业务处理参数或成员，类型为 {@code int}
     */
    private void work(OperationsEvent e, String type, String business, int assignee) {
        mapper.insertWork(ids.nextId(), type, e.supplierId(), business, e.businessId(), e.businessNo(), e.message(), assignee, e.dueAt(), e.eventCode());
    }

    /**
     * 处理当前类型职责中的操作 {@code warning}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param e 业务处理参数或成员，类型为 {@code OperationsEvent}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param business 业务处理参数或成员，类型为 {@code String}
     * @param level 业务处理参数或成员，类型为 {@code int}
     */
    private void warning(OperationsEvent e, String type, String business, int level) {
        mapper.insertWarning(ids.nextId(), type, e.supplierId(), business, e.businessId(), level, e.message(), e.occurredAt(), e.eventCode());
    }

    /**
     * 校验业务约束 {@code check}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param p 业务处理参数或成员，类型为 {@code int}
     * @param s 业务处理参数或成员，类型为 {@code int}
     */
    private static void check(int p, int s) {
        if (p < 1 || s < 1 || s > CHECK_VALUE_100) {
            throw rule("分页参数不合法");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param m 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String m) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, m);
    }

    /**
     * 处理当前类型职责中的操作 {@code conflict}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException conflict() {
        return new BusinessException(ErrorCode.VERSION_CONFLICT, "状态或版本已变更");
    }

    /**
     * 业务常量 {@code CHECK_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CHECK_VALUE_100 = 100;

    /**
     * 业务常量 {@code FAILED_EVENT}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String FAILED_EVENT = "FAILED_EVENT";

    /**
     * 业务常量 {@code QUALITY}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String QUALITY = "QUALITY";

    /**
     * 业务常量 {@code RECONCILIATION}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String RECONCILIATION = "RECONCILIATION";

    /**
     * 业务常量 {@code RETURN}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String RETURN = "RETURN";

    /**
     * 业务常量 {@code SCORE}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String SCORE = "SCORE";

    /**
     * 业务常量 {@code WARNING}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String WARNING = "WARNING";

    /**
     * 业务常量 {@code WORK_ITEM}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String WORK_ITEM = "WORK_ITEM";

    /**
     * 业务常量 {@code PROCESS_WARNING_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PROCESS_WARNING_VALUE_2 = 2;

    /**
     * 业务常量 {@code PROCESS_WARNING_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PROCESS_WARNING_VALUE_3 = 3;

    /**
     * 业务常量 {@code PROCESS_WORK_VALUE_4}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PROCESS_WORK_VALUE_4 = 4;

    /**
     * 导出任务已完成状态。
     */
    private static final int EXPORT_COMPLETED_STATUS = 3;
}
