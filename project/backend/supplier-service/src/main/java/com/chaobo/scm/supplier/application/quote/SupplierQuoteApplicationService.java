package com.chaobo.scm.supplier.application.quote;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.masterdata.MasterDataSnapshotPort;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.quote.*;
import com.chaobo.scm.supplier.domain.shared.*;
import com.chaobo.scm.supplier.infrastructure.persistence.rfq.SupplierQuoteTodoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/**
 * SupplierQuoteApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierQuoteApplicationService {

    /**
     * repo（类型：{@code SupplierQuoteRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQuoteRepository repo;

    /**
     * read（类型：{@code SupplierQuoteReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQuoteReadModelPort read;

    /**
     * master（类型：{@code MasterDataSnapshotPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataSnapshotPort master;

    /**
     * todos（类型：{@code SupplierQuoteTodoMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQuoteTodoMapper todos;

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
     * 创建 SupplierQuoteApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code SupplierQuoteRepository}
     * @param read 业务处理参数或成员，类型为 {@code SupplierQuoteReadModelPort}
     * @param master 业务处理参数或成员，类型为 {@code MasterDataSnapshotPort}
     * @param todos 业务处理参数或成员，类型为 {@code SupplierQuoteTodoMapper}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     */
    public SupplierQuoteApplicationService(SupplierQuoteRepository repo, SupplierQuoteReadModelPort read, MasterDataSnapshotPort master, SupplierQuoteTodoMapper todos, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor) {
        this.repo = repo;
        this.read = read;
        this.master = master;
        this.todos = todos;
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
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code com.chaobo.scm.common.api.PageResult<SupplierQuoteView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public com.chaobo.scm.common.api.PageResult<SupplierQuoteView> page(Long supplierId, Long scope, Integer status, String keyword, int page, int size) {
        if (page < 1 || size < 1 || size > PAGE_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        return read.page(scope == null ? supplierId : scope, status, keyword, page, size);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierQuoteView detail(long id, Long scope) {
        var value = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "报价不存在"));
        if (scope != null && scope != value.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报价不存在");
        }
        return value;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param rfqId 业务或技术标识，类型为 {@code Long}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(long supplierId, Long rfqId, String rfqNo, String currency, LocalDate from, LocalDate to, List<QuoteLine> lines, CommandContext c) {
        c.requirePermission("supplier:quote:create");
        c.requireSupplierScope(supplierId);
        return executor.execute("supplier:quote", c, new Create(supplierId, rfqId, rfqNo, currency, from, to, lines), () -> {
            var normalized = normalizeNewLineIds(lines);
            eligible(supplierId, normalized);
            return save(SupplierQuoteAggregate.create(supplierId, rfqId, rfqNo, currency, from, to, normalized, c.operatorId(), ids), c, "CREATE_SUPPLIER_QUOTE", null);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code modifyDraft}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult modifyDraft(long id, int version, LocalDate from, LocalDate to, List<QuoteLine> lines, CommandContext c) {
        c.requirePermission("supplier:quote:update");
        return act(id, version, "MODIFY_SUPPLIER_QUOTE_DRAFT", c, a -> {
            eligible(a.supplierId(), lines);
            a.modifyDraft(from, to, normalizeLineIds(a, lines), c.operatorId(), ids);
        });
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult submit(long id, int version, CommandContext c) {
        c.requirePermission("supplier:quote:submit");
        return act(id, version, "SUBMIT_SUPPLIER_QUOTE", c, a -> {
            eligible(a.supplierId(), a.lines());
            assertRfqOpen(a);
            a.submit(c.operatorId(), ids);
            if (a.rfqId() != null && todos.markSubmitted(a.rfqId(), a.supplierId()) != 1) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "询价已截标或报价待办不可提交");
            }
        });
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult confirm(long id, int version, CommandContext c) {
        c.requirePermission("supplier:quote:confirm");
        return act(id, version, "CONFIRM_SUPPLIER_QUOTE", c, a -> a.confirm(c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code adopt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param ref 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult adopt(long id, int version, String ref, CommandContext c) {
        c.requirePermission("supplier:quote:adopt");
        return act(id, version, "ADOPT_SUPPLIER_QUOTE", c, a -> a.adopt(ref, c.operatorId(), ids));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult reject(long id, int version, String reason, CommandContext c) {
        c.requirePermission("supplier:quote:reject");
        return act(id, version, "REJECT_SUPPLIER_QUOTE", c, a -> a.reject(reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code voidQuote}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult voidQuote(long id, int version, String reason, CommandContext c) {
        c.requirePermission("supplier:quote:void");
        return act(id, version, "VOID_SUPPLIER_QUOTE", c, a -> a.voidQuote(reason, c.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code expireQuotes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Transactional(rollbackFor = Exception.class)
    public void expireQuotes() {
        for (long id : repo.expiredIds()) {
            var a = repo.findById(id).orElse(null);
            if (a == null) {
                continue;
            }
            String before = snapshot(a);
            a.expire(0, ids);
            if (a.status() == QuoteStatus.EXPIRED) {
                var c = new CommandContext(0, "系统", 0, null, "quote-expire-" + id, null, "quote-expire-" + id, Set.of());
                save(a, c, "EXPIRE_SUPPLIER_QUOTE", before);
            }
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code act}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param f 业务处理参数或成员，类型为 {@code java.util.function.Consumer<SupplierQuoteAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult act(long id, int version, String op, CommandContext c, java.util.function.Consumer<SupplierQuoteAggregate> f) {
        return executor.execute("supplier:quote", c, new Change(id, version, op), () -> {
            var a = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商报价不存在"));
            c.requireSupplierScope(a.supplierId());
            if (a.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "报价已被更新");
            }
            String before = snapshot(a);
            f.accept(a);
            return save(a, c, op, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code normalizeNewLineIds}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<QuoteLine>}
     */
    private List<QuoteLine> normalizeNewLineIds(List<QuoteLine> lines) {
        return lines.stream().map(line -> new QuoteLine(ids.nextId(), line.skuCode(), line.quoteQty(), line.unitPrice(), line.taxRate(), line.deliveryDays(), line.moq())).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code normalizeLineIds}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<QuoteLine>}
     */
    private List<QuoteLine> normalizeLineIds(SupplierQuoteAggregate a, List<QuoteLine> lines) {
        var existing = a.lines().stream().collect(java.util.stream.Collectors.toMap(QuoteLine::lineId, java.util.function.Function.identity()));
        var result = new ArrayList<QuoteLine>();
        for (var line : lines) {
            long lineId = line.lineId();
            if (lineId <= 0) {
                lineId = ids.nextId();
            } else if (!existing.containsKey(lineId)) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "报价行不属于当前报价，新增行请勿传入行ID");
            }
            result.add(new QuoteLine(lineId, line.skuCode(), line.quoteQty(), line.unitPrice(), line.taxRate(), line.deliveryDays(), line.moq()));
        }
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code assertRfqOpen}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     */
    private void assertRfqOpen(SupplierQuoteAggregate a) {
        if (a.rfqId() == null) {
            return;
        }
        var todo = todos.state(a.rfqId(), a.supplierId());
        boolean todoMissing = todo == null;
        boolean todoClosed = !todoMissing && todo.status() != 1;
        boolean deadlinePassed = !todoMissing && todo.deadline() != null && todo.deadline().isBefore(OffsetDateTime.now());
        if (todoMissing || todoClosed || deadlinePassed) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "询价已截标或报价待办不可提交");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code eligible}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param lines 业务处理参数或成员，类型为 {@code List<QuoteLine>}
     */
    private void eligible(long supplierId, List<QuoteLine> lines) {
        var supplier = master.findSupplier(supplierId).orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商主数据快照不存在"));
        if (!supplier.enabled()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商未启用，不能报价");
        }
        for (var line : lines) {
            var sku = master.findSku(line.skuCode()).orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "SKU主数据快照不存在: " + line.skuCode()));
            if (!sku.enabled()) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "SKU已停用，不能报价: " + line.skuCode());
            }
        }
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @param op 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    private CommandResult save(SupplierQuoteAggregate a, CommandContext c, String op, String before) {
        repo.save(a, c.operatorId());
        var events = a.pullEvents();
        outbox.saveAll(events);
        audit.save(c, op, "SUPPLIER_QUOTE", a.id(), a.no(), before, snapshot(a));
        String e = events.isEmpty() ? null : events.get(events.size() - 1).eventCode();
        return new CommandResult(a.id(), a.no(), a.status().code(), a.status().label(), a.version(), e, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierQuoteAggregate a) {
        return "{\"quoteNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(a.no(), a.status().code(), a.version());
    }

    /**
     * Create。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Create(long supplierId, Long rfqId, String rfqNo, String currency, LocalDate from, LocalDate to, List<QuoteLine> lines) {
    }

    /**
     * Change。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Change(long id, int version, String op) {
    }

    /**
     * 业务常量 {@code PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PAGE_VALUE_100 = 100;
}
