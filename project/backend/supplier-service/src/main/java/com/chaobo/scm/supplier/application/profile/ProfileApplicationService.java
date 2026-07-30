package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.profile.*;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ProfileApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class ProfileApplicationService {

    /**
     * repository（类型：{@code ProfileChangeRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ProfileChangeRepository repository;

    /**
     * readModel（类型：{@code ProfileReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ProfileReadModelPort readModel;

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
     * 创建 ProfileApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code ProfileChangeRepository}
     * @param readModel 业务处理参数或成员，类型为 {@code ProfileReadModelPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     */
    public ProfileApplicationService(ProfileChangeRepository repository, ProfileReadModelPort readModel, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor) {
        this.repository = repository;
        this.readModel = readModel;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
        this.executor = executor;
    }

    /**
     * 处理当前类型职责中的操作 {@code profile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param scopeId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ProfileViews.Profile}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ProfileViews.Profile profile(long supplierId, Long scopeId) {
        requireScope(supplierId, scopeId);
        return readModel.findProfile(supplierId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商档案不存在"));
    }

    /**
     * 处理当前类型职责中的操作 {@code changes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param scopeId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<ProfileViews.Change>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<ProfileViews.Change> changes(long supplierId, Long scopeId, Integer status, int pageNo, int pageSize) {
        requireScope(supplierId, scopeId);
        validatePage(pageNo, pageSize);
        return readModel.pageChanges(supplierId, status, pageNo, pageSize);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param profileVersion 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param changes 业务处理参数或成员，类型为 {@code List<ProfileFieldChange>}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(long supplierId, int profileVersion, String reason, List<ProfileFieldChange> changes, CommandContext context) {
        context.requirePermission("supplier:supplier_profile:change");
        context.requireSupplierScope(supplierId);
        var request =
                new SubmitChangeRequest(supplierId, profileVersion, reason, changes);
        return executor.execute("supplier:profile", context, request, () -> {
            var profile = readModel.findProfile(supplierId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商档案不存在"));
            if (profile.version() != profileVersion) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "档案版本已变化");
            }
            if (repository.existsPending(supplierId)) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "已有待审批资料变更");
            }
            var aggregate = ProfileChangeAggregate.submit(supplierId, profileVersion, reason, changes, context.operatorId(), ids);
            return persist(aggregate, context, "SUBMIT_PROFILE_CHANGE", null);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code withdraw}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param changeId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult withdraw(long changeId, int version, String reason, CommandContext context) {
        context.requirePermission("supplier:supplier_profile:change");
        var request = new WithdrawChangeRequest(changeId, version, reason);
        return executor.execute("supplier:profile", context, request, () -> {
            var aggregate = repository.findById(changeId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资料变更不存在"));
            context.requireSupplierScope(aggregate.supplierId());
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "资料变更版本已变化");
            }
            String before = snapshot(aggregate);
            aggregate.withdraw(reason, context.operatorId(), ids);
            return persist(aggregate, context, "WITHDRAW_PROFILE_CHANGE", before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code ProfileChangeAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(ProfileChangeAggregate aggregate, CommandContext context, String operation, String before) {
        repository.save(aggregate, context.operatorId());
        List<DomainEvent> events = aggregate.pullEvents();
        outbox.saveAll(events);
        audit.save(context, operation, "SUPPLIER_PROFILE_CHANGE", aggregate.changeId(), aggregate.changeNo(), before, snapshot(aggregate));
        String event = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(aggregate.changeId(), aggregate.changeNo(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), event, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code ProfileChangeAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(ProfileChangeAggregate aggregate) {
        return "{\"changeNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.changeNo(), aggregate.status().code(), aggregate.version());
    }

    /**
     * 查询并返回 {@code requireScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param scopeId 业务或技术标识，类型为 {@code Long}
     */
    private void requireScope(long supplierId, Long scopeId) {
        if (scopeId != null && scopeId != supplierId) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商档案不存在");
        }
    }

    /**
     * 校验业务约束 {@code validatePage}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     */
    private void validatePage(int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > VALIDATE_PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
    }

    /**
     * 提交档案变更时使用的幂等请求快照。
     *
     * <p>绑定供应商档案版本和字段差异，防止重复请求生成多张审批单。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record SubmitChangeRequest(
            long supplierId,
            int profileVersion,
            String reason,
            List<ProfileFieldChange> changes) {
    }

    /**
     * 撤回档案变更时使用的幂等请求快照。
     *
     * <p>记录变更单、乐观锁版本和撤回原因，使命令重放仍保持相同业务语义。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record WithdrawChangeRequest(long changeId, int version, String reason) {
    }

    /**
     * 业务常量 {@code VALIDATE_PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VALIDATE_PAGE_VALUE_100 = 100;
}
