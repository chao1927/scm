package com.chaobo.scm.supplier.application.contract;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.masterdata.MasterDataSnapshotPort;
import com.chaobo.scm.supplier.application.qualification.SupplierQualificationPolicyPort;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.contract.*;
import com.chaobo.scm.supplier.domain.quote.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * SupplierContractApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierContractApplicationService {

    /**
     * contracts（类型：{@code SupplierContractRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractRepository contracts;

    /**
     * read（类型：{@code SupplierContractReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractReadModelPort read;

    /**
     * quotes（类型：{@code SupplierQuoteRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQuoteRepository quotes;

    /**
     * agreements（类型：{@code PriceAgreementProjectionPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PriceAgreementProjectionPort agreements;

    /**
     * masterData（类型：{@code MasterDataSnapshotPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataSnapshotPort masterData;

    /**
     * qualifications（类型：{@code SupplierQualificationPolicyPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualificationPolicyPort qualifications;

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
     * 创建 SupplierContractApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param contracts 业务处理参数或成员，类型为 {@code SupplierContractRepository}
     * @param read 业务处理参数或成员，类型为 {@code SupplierContractReadModelPort}
     * @param quotes 业务处理参数或成员，类型为 {@code SupplierQuoteRepository}
     * @param agreements 业务处理参数或成员，类型为 {@code PriceAgreementProjectionPort}
     * @param masterData 业务处理参数或成员，类型为 {@code MasterDataSnapshotPort}
     * @param qualifications 业务处理参数或成员，类型为 {@code SupplierQualificationPolicyPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param executor 业务处理参数或成员，类型为 {@code TransactionalCommandExecutor}
     */
    public SupplierContractApplicationService(SupplierContractRepository contracts, SupplierContractReadModelPort read, SupplierQuoteRepository quotes, PriceAgreementProjectionPort agreements, MasterDataSnapshotPort masterData, SupplierQualificationPolicyPort qualifications, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, TransactionalCommandExecutor executor) {
        this.contracts = contracts;
        this.read = read;
        this.quotes = quotes;
        this.agreements = agreements;
        this.masterData = masterData;
        this.qualifications = qualifications;
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
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierContractView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<SupplierContractView> page(Long supplierId, Long scope, Integer status, String keyword, int pageNo, int pageSize) {
        validatePage(pageNo, pageSize);
        return read.page(scope == null ? supplierId : scope, status, keyword, pageNo, pageSize);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierContractView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierContractView detail(long id, Long scope) {
        var value = read.detail(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "合同不存在"));
        if (scope != null && scope != value.supplierId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "合同不存在");
        }
        return value;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param quoteId 业务或技术标识，类型为 {@code Long}
     * @param agreement 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param terms 业务处理参数或成员，类型为 {@code String}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult create(long supplierId, Long quoteId, String agreement, String type, LocalDate from, LocalDate to, String terms, String attachment, CommandContext context) {
        context.requirePermission("supplier:contract:create");
        context.requireSupplierScope(supplierId);
        return executor.execute("supplier:contract", context, new Create(supplierId, quoteId, agreement, type, from, to), () -> {
            ensureSupplierEligible(supplierId);
            return persist(SupplierContractAggregate.create(supplierId, quoteId, agreement, type, from, to, terms, attachment, context.operatorId(), ids), context, "CREATE_CONTRACT", null);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code modifyDraft}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param until 业务处理参数或成员，类型为 {@code LocalDate}
     * @param terms 业务处理参数或成员，类型为 {@code String}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult modifyDraft(long id, int version, LocalDate until, String terms, String attachment, CommandContext context) {
        context.requirePermission("supplier:contract:update");
        return change(id, version, "MODIFY_CONTRACT_DRAFT", context, a -> a.modifyDraft(until, terms, attachment, context.operatorId(), ids));
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
        context.requirePermission("supplier:contract:submit");
        return change(id, version, "SUBMIT_CONTRACT", context, a -> {
            ensureSupplierEligible(a.supplierId());
            a.submit(context.operatorId(), ids);
        });
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
        context.requirePermission("supplier:contract:approve");
        return change(id, version, "APPROVE_CONTRACT", context, a -> {
            ensureSupplierEligible(a.supplierId());
            var quote = adoptedQuote(a);
            a.approve(context.operatorId(), ids);
            if (quote != null) {
                agreements.activate(a, quote);
            }
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code activate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult activate(long id, int version, CommandContext context) {
        return approve(id, version, context);
    }

    /**
     * 执行命令 {@code rejectApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult rejectApproval(long id, int version, String comment, CommandContext context) {
        context.requirePermission("supplier:contract:approve");
        return change(id, version, "REJECT_CONTRACT_APPROVAL", context, a -> a.rejectApproval(comment, context.operatorId(), ids));
    }

    /**
     * 处理当前类型职责中的操作 {@code renew}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param until 业务处理参数或成员，类型为 {@code LocalDate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult renew(long id, int version, LocalDate until, CommandContext context) {
        context.requirePermission("supplier:contract:renew");
        return change(id, version, "RENEW_CONTRACT", context, a -> {
            a.renew(until, context.operatorId(), ids);
            if (a.quoteId() != null) {
                agreements.renew(a);
            }
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code terminate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult terminate(long id, int version, String reason, CommandContext context) {
        context.requirePermission("supplier:contract:terminate");
        return change(id, version, "TERMINATE_CONTRACT", context, a -> {
            a.terminate(reason, context.operatorId(), ids);
            if (a.quoteId() != null) {
                agreements.terminate(a);
            }
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param action 业务处理参数或成员，类型为 {@code Consumer<SupplierContractAggregate>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult change(long id, int version, String operation, CommandContext context, Consumer<SupplierContractAggregate> action) {
        return executor.execute("supplier:contract", context, new Change(id, version, operation), () -> {
            var aggregate = contracts.find(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "合同不存在"));
            context.requireSupplierScope(aggregate.supplierId());
            if (aggregate.version() != version) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "合同已更新");
            }
            String before = snapshot(aggregate);
            action.accept(aggregate);
            return persist(aggregate, context, operation, before);
        });
    }

    /**
     * 处理当前类型职责中的操作 {@code adoptedQuote}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param contract 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteAggregate}
     */
    private SupplierQuoteAggregate adoptedQuote(SupplierContractAggregate contract) {
        if (contract.quoteId() == null) {
            return null;
        }
        if (contract.agreement() == null || contract.agreement().isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "价格协议编号不能为空");
        }
        var quote = quotes.findById(contract.quoteId()).orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "关联报价不存在"));
        if (quote.status() != QuoteStatus.ADOPTED) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "只有已采纳报价可以生成价格协议");
        }
        return quote;
    }

    /**
     * 校验业务约束 {@code ensureSupplierEligible}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     */
    private void ensureSupplierEligible(long supplierId) {
        var supplier = masterData.findSupplier(supplierId).orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商主数据快照不存在"));
        if (!supplier.enabled()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商未启用，不能处理合同");
        }
        if (!qualifications.hasValidQualification(supplierId)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商不存在有效资质，不能处理合同");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code persist}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param before 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult persist(SupplierContractAggregate aggregate, CommandContext context, String operation, String before) {
        contracts.save(aggregate, context.operatorId());
        var events = aggregate.pullEvents();
        outbox.saveAll(events);
        audit.save(context, operation, "SUPPLIER_CONTRACT", aggregate.id(), aggregate.no(), before, snapshot(aggregate));
        return new CommandResult(aggregate.id(), aggregate.no(), aggregate.status().code(), aggregate.status().label(), aggregate.version(), events.isEmpty() ? null : events.get(events.size() - 1).eventCode(), false);
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
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(SupplierContractAggregate a) {
        return "{\"contractNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(a.no(), a.status().code(), a.version());
    }

    /**
     * Create。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private record Create(long supplierId, Long quoteId, String agreement, String type, LocalDate from, LocalDate to) {
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
     * 业务常量 {@code VALIDATE_PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VALIDATE_PAGE_VALUE_100 = 100;
}
