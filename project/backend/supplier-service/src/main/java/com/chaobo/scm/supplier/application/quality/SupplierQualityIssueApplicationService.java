package com.chaobo.scm.supplier.application.quality;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.quality.*;
import com.chaobo.scm.supplier.domain.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.function.*;

/**
 * SupplierQualityIssueApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierQualityIssueApplicationService {

    /**
     * repo（类型：{@code SupplierQualityIssueRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualityIssueRepository repo;

    /**
     * read（类型：{@code SupplierQualityIssueReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualityIssueReadModelPort read;

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
     * 创建 SupplierQualityIssueApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code SupplierQualityIssueRepository}
     * @param read 业务处理参数或成员，类型为 {@code SupplierQualityIssueReadModelPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     */
    public SupplierQualityIssueApplicationService(SupplierQualityIssueRepository repo, SupplierQualityIssueReadModelPort read, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor) {
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
     * @param severity 业务处理参数或成员，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierQualityIssueView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<SupplierQualityIssueView> page(Long supplierId, Long scope, Integer status, Integer severity, int page, int size) {
        if (page < 1 || size < 1 || size > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return read.page(scope == null ? supplierId : scope, status, severity, page, size);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualityIssueView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierQualityIssueView detail(long id, Long scope) {
        var v = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "质量问题不存在"));
        if (scope != null && scope != v.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "质量问题不存在");
        }
        return v;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param issueType 业务处理参数或成员，类型为 {@code String}
     * @param severity 业务处理参数或成员，类型为 {@code int}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(long supplierId, String sourceType, String sourceNo, String issueType, int severity, String description, CommandContext c) {
        c.requirePermission("supplier:quality:create");
        c.requireSupplierScope(supplierId);
        return executor.execute("supplier:quality", c, new Create(supplierId, sourceType, sourceNo, issueType, severity, description), () -> persist(SupplierQualityIssueAggregate.create(supplierId, sourceType, sourceNo, issueType, severity, description, c.operatorId(), ids), c, "CREATE_QUALITY_ISSUE", null));
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRectification}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param deadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult requestRectification(long id, int version, OffsetDateTime deadline, CommandContext c) {
        c.requirePermission("supplier:quality:rectify");
        return change(id, version, "REQUEST_RECTIFICATION", c, a -> a.requestRectification(deadline, c.operatorId(), ids));
    }

    /**
     * 执行命令 {@code submitPlan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param plan 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submitPlan(long id, int version, String plan, CommandContext c) {
        c.requirePermission("supplier:quality:submit_plan");
        return change(id, version, "SUBMIT_RECTIFICATION", c, a -> a.submitPlan(plan, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code verify}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param passed 业务处理参数或成员，类型为 {@code boolean}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult verify(long id, int version, boolean passed, String comment, CommandContext c) {
        c.requirePermission("supplier:quality:verify");
        return change(id, version, "VERIFY_RECTIFICATION", c, a -> a.verify(passed, comment, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code markOverdue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Transactional(rollbackFor = Exception.class)
    public void markOverdue() {
        for (long id : read.overdueIds()) {
            var a = repo.findById(id).orElse(null);
            if (a == null) {
                continue;
            }
            String before = snapshot(a);
            a.markOverdue(0, ids);
            if (a.status() == QualityIssueStatus.OVERDUE) {
                var c = new CommandContext(0, "系统", 0, null, "quality-overdue-" + id, null, "quality-overdue-" + id, Set.of());
                persist(a, c, "MARK_RECTIFICATION_OVERDUE", before);
            }
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
     * @param action 业务处理参数或成员，类型为 {@code Consumer<SupplierQualityIssueAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, String op, CommandContext c, Consumer<SupplierQualityIssueAggregate> action) {
        return executor.execute("supplier:quality", c, new Change(id, version, op), () -> {
            var a = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "质量问题不存在"));
            c.requireSupplierScope(a.supplierId());
            if (a.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "质量问题已被更新");
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
     * @param a 业务处理参数或成员，类型为 {@code SupplierQualityIssueAggregate}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(SupplierQualityIssueAggregate a, CommandContext c, String op, String before) {
        repo.save(a, c.operatorId());
        var events = a.pullEvents();
        outbox.saveAll(events);
        audit.save(c, op, "SUPPLIER_QUALITY_ISSUE", a.id(), a.no(), before, snapshot(a));
        return new CommandResult(a.id(), a.no(), a.status().code(), a.status().label(), a.version(), events.isEmpty() ? null : events.get(events.size() - 1).eventCode(), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQualityIssueAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierQualityIssueAggregate a) {
        return "{\"issueNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(a.no(), a.status().code(), a.version());
    }

    /**
     * Create。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Create(long supplierId, String sourceType, String sourceNo, String issueType, int severity, String description) {
    }

    /**
     * Change。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Change(long id, int version, String operation) {
    }

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
