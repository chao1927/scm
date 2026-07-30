package com.chaobo.scm.supplier.application.qualification;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.qualification.*;
import com.chaobo.scm.supplier.domain.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.function.*;

/**
 * SupplierQualificationApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierQualificationApplicationService {

    /**
     * repo（类型：{@code SupplierQualificationRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualificationRepository repo;

    /**
     * read（类型：{@code SupplierQualificationReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualificationReadModelPort read;

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
     * 创建 SupplierQualificationApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code SupplierQualificationRepository}
     * @param read 业务处理参数或成员，类型为 {@code SupplierQualificationReadModelPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     */
    public SupplierQualificationApplicationService(SupplierQualificationRepository repo, SupplierQualificationReadModelPort read, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor) {
        this.repo = repo;
        this.read = read;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
        this.executor = executor;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierQualificationView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<SupplierQualificationView> page(Long supplierId, Long scope, Integer status, int page, int size) {
        if (page < 1 || size < 1 || size > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return read.page(scope == null ? supplierId : scope, status, page, size);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualificationView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierQualificationView detail(long id, Long scope) {
        var v = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商资质不存在"));
        if (scope != null && scope != v.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商资质不存在");
        }
        return v;
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param number 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(long supplierId, String type, String number, LocalDate from, LocalDate to, String attachment, CommandContext context) {
        context.requirePermission("supplier:qualification:submit");
        context.requireSupplierScope(supplierId);
        return executor.execute("supplier:qualification", context, type + number + to, () -> persist(SupplierQualificationAggregate.submit(supplierId, type, number, from, to, attachment, context.operatorId(), ids), context, "SUBMIT_QUALIFICATION", null));
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult approve(long id, int version, String remark, CommandContext context) {
        context.requirePermission("supplier:qualification:approve");
        return change(id, version, "APPROVE_QUALIFICATION", context, a -> a.approve(remark, context.operatorId(), ids));
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
        context.requirePermission("supplier:qualification:approve");
        return change(id, version, "REJECT_QUALIFICATION", context, a -> a.reject(reason, context.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code expire}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public void expire(long id) {
        var a = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商资质不存在"));
        String before = snapshot(a);
        a.expire(0, ids);
        if (a.status() == QualificationStatus.EXPIRED) {
            persist(a, new CommandContext(0, "系统", 0, null, "qualification-expire-" + id, null, "qualification-expire-" + id, java.util.Set.of()), "EXPIRE_QUALIFICATION", before);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param action 业务处理参数或成员，类型为 {@code Consumer<SupplierQualificationAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, String op, CommandContext c, Consumer<SupplierQualificationAggregate> action) {
        return executor.execute("supplier:qualification", c, id + ":" + version + op, () -> {
            var a = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商资质不存在"));
            c.requireSupplierScope(a.supplierId());
            if (a.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "资质已被更新");
            }
            String before = snapshot(a);
            action.accept(a);
            return persist(a, c, op, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQualificationAggregate}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(SupplierQualificationAggregate a, CommandContext c, String op, String before) {
        repo.save(a, c.operatorId());
        var events = a.pullEvents();
        outbox.saveAll(events);
        audit.save(c, op, "SUPPLIER_QUALIFICATION", a.id(), Long.toString(a.id()), before, snapshot(a));
        return new CommandResult(a.id(), Long.toString(a.id()), a.status().code(), a.status().label(), a.version(), events.isEmpty() ? null : events.get(events.size() - 1).eventCode(), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQualificationAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierQualificationAggregate a) {
        return "{\"qualificationId\":%d,\"status\":%d,\"validTo\":\"%s\",\"version\":%d}".formatted(a.id(), a.status().code(), a.to(), a.version());
    }

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
