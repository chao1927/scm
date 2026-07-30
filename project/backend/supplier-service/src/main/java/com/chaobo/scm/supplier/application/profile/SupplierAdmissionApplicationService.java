package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.common.integration.MasterDataCollaborationApi;
import com.chaobo.scm.supplier.application.integration.IntegrationCommandEnqueuer;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.profile.*;
import com.chaobo.scm.supplier.domain.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;

/**
 * SupplierAdmissionApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierAdmissionApplicationService {

    /**
     * repo（类型：{@code SupplierAdmissionRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierAdmissionRepository repo;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * executor（类型：{@code TransactionalCommandExecutor}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final TransactionalCommandExecutor executor;

    /**
     * integrations（类型：{@code IntegrationCommandEnqueuer}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandEnqueuer integrations;

    /**
     * 创建 SupplierAdmissionApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code SupplierAdmissionRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     * @param integrations 业务处理参数或成员，类型为 {@code IntegrationCommandEnqueuer}
     */
    public SupplierAdmissionApplicationService(SupplierAdmissionRepository repo, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor, IntegrationCommandEnqueuer integrations) {
        this.repo = repo;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
        this.executor = executor;
        this.integrations = integrations;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param tax 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param contact 业务处理参数或成员，类型为 {@code String}
     * @param mobile 业务处理参数或成员，类型为 {@code String}
     * @param settlement 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(String code, String name, String tax, String type, String contact, String mobile, String settlement, CommandContext context) {
        context.requirePermission("supplier:admission:create");
        return executor.execute("supplier:admission", context, code + tax, () -> save(SupplierAdmissionAggregate.create(code, name, tax, type, contact, mobile, settlement, context.operatorId(), ids), context, "CREATE_ADMISSION", null));
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
        context.requirePermission("supplier:admission:submit");
        return change(id, version, "SUBMIT_ADMISSION", context, aggregate -> aggregate.submit(context.operatorId(), ids));
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult approve(long id, int version, CommandContext context) {
        context.requirePermission("supplier:admission:approve");
        return change(id, version, "APPROVE_ADMISSION", context, aggregate -> aggregate.approve(context.operatorId(), ids));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult reject(long id, int version, String reason, CommandContext context) {
        context.requirePermission("supplier:admission:approve");
        return change(id, version, "REJECT_ADMISSION", context, aggregate -> aggregate.reject(reason, context.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param action 业务处理参数或成员，类型为 {@code Consumer<SupplierAdmissionAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, String operation, CommandContext context, Consumer<SupplierAdmissionAggregate> action) {
        return executor.execute("supplier:admission", context, id + ":" + version + operation, () -> {
            var aggregate = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "准入单不存在"));
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "准入单已更新");
            }
            String before = snapshot(aggregate);
            action.accept(aggregate);
            return save(aggregate, context, operation, before);
        });
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierAdmissionAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    private CommandResult save(SupplierAdmissionAggregate aggregate, CommandContext context, String operation, String before) {
        repo.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        if (APPROVE_ADMISSION.equals(operation)) {
            integrations.enqueue("MDM_CREATE_SUPPLIER", "SUPPLIER_ADMISSION", aggregate.id(), aggregate.version(), "MDM", new MasterDataCollaborationApi.CreateSupplierCommand("MDM-SUPPLIER-" + aggregate.id() + "-" + aggregate.version(), aggregate.id(), aggregate.no(), aggregate.code(), aggregate.name(), aggregate.taxNo(), aggregate.type(), aggregate.contactName(), aggregate.contactMobile(), aggregate.settlementJson()));
        }
        audit.save(context, operation, "SUPPLIER_ADMISSION", aggregate.id(), aggregate.no(), before, snapshot(aggregate));
        return new CommandResult(aggregate.id(), aggregate.no(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), events.isEmpty() ? null : events.get(events.size() - 1).eventCode(), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierAdmissionAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierAdmissionAggregate aggregate) {
        return "{\"admissionNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.no(), aggregate.status().code(), aggregate.version());
    }

    /**
     * 业务常量 {@code APPROVE_ADMISSION}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String APPROVE_ADMISSION = "APPROVE_ADMISSION";
}
