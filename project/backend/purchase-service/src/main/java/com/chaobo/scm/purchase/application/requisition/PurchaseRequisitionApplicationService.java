package com.chaobo.scm.purchase.application.requisition;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.shared.IdempotencyPort;
import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionAggregate;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionLine;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionRepository;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * PurchaseRequisitionApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseRequisitionApplicationService {

    /**
     * repository（类型：{@code PurchaseRequisitionRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseRequisitionRepository repository;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * auditLog（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository auditLog;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * idempotency（类型：{@code IdempotencyPort}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdempotencyPort idempotency;

    /**
     * 创建 PurchaseRequisitionApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code PurchaseRequisitionRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param auditLog 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param idempotency 业务或技术标识，类型为 {@code IdempotencyPort}
     */
    public PurchaseRequisitionApplicationService(PurchaseRequisitionRepository repository, OutboxRepository outbox, AuditLogRepository auditLog, IdentifierGenerator ids, IdempotencyPort idempotency) {
        this.repository = repository;
        this.outbox = outbox;
        this.auditLog = auditLog;
        this.ids = ids;
        this.idempotency = idempotency;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code PurchaseRequisitionCommands.Save}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(PurchaseRequisitionCommands.Save command, CommandContext context) {
        context.requirePermission("purchase:requisition:create");
        context.requirePurchaseOrgScope(command.purchaseOrgId());
        return idempotency.execute("purchase:requisition:create", context, () -> {
            var aggregate = PurchaseRequisitionAggregate.create(command.applicantId(), command.purchaseOrgId(), command.demandDepartmentId(), command.reason(), lines(command.lines()), ids);
            return persist(aggregate, context, "CREATE_REQUISITION", null);
        });
    }

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code PurchaseRequisitionCommands.Save}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult update(long id, PurchaseRequisitionCommands.Save command, CommandContext context) {
        context.requirePermission("purchase:requisition:update");
        return idempotency.execute("purchase:requisition:update", context, () -> {
            var aggregate = load(id);
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            checkVersion(aggregate, command.version());
            var before = snapshot(aggregate);
            aggregate.changeDraft(command.reason(), lines(command.lines()), ids);
            return persist(aggregate, context, "UPDATE_REQUISITION", before);
        });
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(long id, int version, CommandContext context) {
        context.requirePermission("purchase:requisition:submit");
        return change(id, version, context, "SUBMIT_REQUISITION", aggregate -> aggregate.submit(ids));
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code PurchaseRequisitionCommands.Approve}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult approve(long id, PurchaseRequisitionCommands.Approve command, CommandContext context) {
        context.requirePermission("purchase:requisition:approve");
        return change(id, command.version(), context, "APPROVE_REQUISITION", aggregate -> aggregate.approve(defaultMap(command.approvedQuantities()), ids));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code PurchaseRequisitionCommands.Reject}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult reject(long id, PurchaseRequisitionCommands.Reject command, CommandContext context) {
        context.requirePermission("purchase:requisition:approve");
        return change(id, command.version(), context, "REJECT_REQUISITION", aggregate -> aggregate.reject(command.reason(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code convert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code PurchaseRequisitionCommands.Convert}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult convert(long id, PurchaseRequisitionCommands.Convert command, CommandContext context) {
        context.requirePermission("purchase:requisition:convert");
        return change(id, command.version(), context, "CONVERT_REQUISITION", aggregate -> aggregate.convert(command.quantities(), command.targetType(), command.targetNo(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code java.util.function.Consumer<PurchaseRequisitionAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, CommandContext context, String operation, java.util.function.Consumer<PurchaseRequisitionAggregate> action) {
        return idempotency.execute("purchase:requisition:" + operation, context, () -> {
            var aggregate = load(id);
            context.requirePurchaseOrgScope(aggregate.purchaseOrgId());
            checkVersion(aggregate, version);
            var before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchaseRequisitionAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(PurchaseRequisitionAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        auditLog.save(context, operation, "PURCHASE_REQUISITION", aggregate.id(), aggregate.requisitionNo(), before, snapshot(aggregate));
        var eventCode = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.id(), aggregate.requisitionNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), eventCode, false);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code PurchaseRequisitionAggregate}
     */
    private PurchaseRequisitionAggregate load(long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请购单不存在"));
    }

    /**
     * 校验业务约束 {@code checkVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchaseRequisitionAggregate}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private void checkVersion(PurchaseRequisitionAggregate aggregate, int version) {
        if (aggregate.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "请购单已被其他人修改");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param commands 用例输入命令，类型为 {@code List<PurchaseRequisitionCommands.Line>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseRequisitionLine>}
     */
    private List<PurchaseRequisitionLine> lines(List<PurchaseRequisitionCommands.Line> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "请购行不能为空");
        }
        return commands.stream().map(line -> new PurchaseRequisitionLine(line.lineId() == null ? ids.nextId() : line.lineId(), line.skuCode(), line.requestedQty(), BigDecimal.ZERO, BigDecimal.ZERO, line.purchaseUnit(), line.requiredDate(), line.remark())).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code defaultMap}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Map<Long,BigDecimal>}
     */
    private static Map<Long, BigDecimal> defaultMap(Map<Long, BigDecimal> value) {
        return value == null ? Map.of() : value;
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchaseRequisitionAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(PurchaseRequisitionAggregate aggregate) {
        return "{\"requisitionNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.requisitionNo(), aggregate.status().code(), aggregate.version());
    }
}
