package com.chaobo.scm.supplier.application.finance;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.finance.SupplierFinanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;

/**
 * SupplierFinanceApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierFinanceApplicationService {

    /**
     * mapper（类型：{@code SupplierFinanceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierFinanceMapper mapper;

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
     * 创建 SupplierFinanceApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierFinanceMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     */
    public SupplierFinanceApplicationService(SupplierFinanceMapper mapper, IdentifierGenerator ids, AuditLogRepository audit) {
        this.mapper = mapper;
        this.ids = ids;
        this.audit = audit;
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciliations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<FinanceViews.Reconciliation>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<FinanceViews.Reconciliation> reconciliations(Long supplierId, Long scope, Integer status, int page, int size) {
        checkPage(page, size);
        Long s = scope == null ? supplierId : scope;
        return new PageResult<>(page, size, mapper.reconciliationCount(s, status), mapper.reconciliations(s, status, (page - 1) * size, size));
    }

    /**
     * 处理当前类型职责中的操作 {@code invoices}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<FinanceViews.Invoice>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<FinanceViews.Invoice> invoices(Long supplierId, Long scope, Integer status, int page, int size) {
        checkPage(page, size);
        Long s = scope == null ? supplierId : scope;
        return new PageResult<>(page, size, mapper.invoiceCount(s, status), mapper.invoices(s, status, (page - 1) * size, size));
    }

    /**
     * 处理当前类型职责中的操作 {@code importStatement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     */
    @Transactional(rollbackFor = Exception.class)
    public void importStatement(String no, long supplierId, String currency, BigDecimal amount, int sourceVersion) {
        if (no == null || no.isBlank() || supplierId <= 0 || currency == null || currency.length() != RECORD_INVOICE_VALIDATION_VALUE_3 || amount == null || amount.signum() < 0) {
            throw rule("对账单数据不合法");
        }
        mapper.upsertStatement(ids.nextId(), no, supplierId, currency, amount, sourceVersion);
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param differenceReason 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirm(long id, int version, BigDecimal amount, String differenceReason, CommandContext c) {
        c.requirePermission("supplier:reconciliation:confirm");
        var row = getReconciliation(id);
        c.requireSupplierScope(row.supplierId());
        if (amount == null || amount.signum() < 0) {
            throw rule("确认金额不合法");
        }
        boolean same = amount.compareTo(row.statementAmount()) == 0;
        boolean differenceReasonMissing = differenceReason == null || differenceReason.isBlank();
        if (!same && differenceReasonMissing) {
            throw rule("对账金额不一致必须填写差异原因");
        }
        if (mapper.respond(id, version, amount, same ? RECORD_INVOICE_VALIDATION_VALUE_2 : RECORD_INVOICE_VALIDATION_VALUE_3, same ? null : differenceReason) != 1) {
            throw conflict();
        }
        audit.save(c, same ? "CONFIRM_RECONCILIATION" : "REPORT_RECONCILIATION_DIFFERENCE", "RECONCILIATION", id, row.statementNo(), null, "{\"status\":" + (same ? 2 : 3) + "}");
    }

    /**
     * 处理当前类型职责中的操作 {@code withdraw}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(long id, int version, CommandContext c) {
        c.requirePermission("supplier:reconciliation:withdraw");
        var row = getReconciliation(id);
        c.requireSupplierScope(row.supplierId());
        if (mapper.changeStatus(id, version, WITHDRAW_VALUE_4) != 1) {
            throw conflict();
        }
        audit.save(c, "WITHDRAW_RECONCILIATION", "RECONCILIATION", id, row.statementNo(), null, "{\"status\":4}");
    }

    /**
     * 处理当前类型职责中的操作 {@code uploadInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param reconciliationId 业务或技术标识，类型为 {@code Long}
     * @param type 业务处理参数或成员，类型为 {@code int}
     * @param net 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param tax 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param url 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public long uploadInvoice(String no, long supplierId, Long reconciliationId, int type, BigDecimal net, BigDecimal tax, BigDecimal rate, String url, CommandContext c) {
        c.requirePermission("supplier:invoice:upload");
        c.requireSupplierScope(supplierId);
        if (no == null || no.isBlank() || type < 1 || type > RECORD_INVOICE_VALIDATION_VALUE_3 || net == null || net.signum() < 0 || tax == null || tax.signum() < 0 || rate == null || rate.signum() < 0 || url == null || url.isBlank()) {
            throw rule("发票数据不合法");
        }
        if (net.multiply(rate).setScale(RECORD_INVOICE_VALIDATION_VALUE_2, RoundingMode.HALF_UP).compareTo(tax.setScale(RECORD_INVOICE_VALIDATION_VALUE_2, RoundingMode.HALF_UP)) != 0) {
            throw rule("税额与不含税金额、税率不匹配");
        }
        long id = ids.nextId();
        mapper.insertInvoice(id, no, supplierId, reconciliationId, type, net, tax, rate, url);
        audit.save(c, "UPLOAD_INVOICE", "INVOICE", id, no, null, "{\"status\":1}");
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code recordInvoiceValidation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param passed 业务处理参数或成员，类型为 {@code boolean}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordInvoiceValidation(long id, boolean passed, String message) {
        var row = mapper.invoice(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "发票不存在");
        }
        boolean validationMessageMissing = message == null || message.isBlank();
        if (!passed && validationMessageMissing) {
            throw rule("校验失败必须说明原因");
        }
        if (mapper.validateInvoice(id, passed ? RECORD_INVOICE_VALIDATION_VALUE_2 : RECORD_INVOICE_VALIDATION_VALUE_3, message) != 1) {
            throw conflict();
        }
    }

    /**
     * 查询并返回 {@code getReconciliation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code FinanceViews.Reconciliation}
     */
    private FinanceViews.Reconciliation getReconciliation(long id) {
        var r = mapper.reconciliation(id);
        if (r == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "对账单不存在");
        }
        return r;
    }

    /**
     * 校验业务约束 {@code checkPage}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param p 业务处理参数或成员，类型为 {@code int}
     * @param s 业务处理参数或成员，类型为 {@code int}
     */
    private static void checkPage(int p, int s) {
        if (p < 1 || s < 1 || s > CHECK_PAGE_VALUE_100) {
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
        return new BusinessException(ErrorCode.VERSION_CONFLICT, "数据状态或版本已变更");
    }

    /**
     * 业务常量 {@code CHECK_PAGE_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CHECK_PAGE_VALUE_100 = 100;

    /**
     * 业务常量 {@code RECORD_INVOICE_VALIDATION_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int RECORD_INVOICE_VALIDATION_VALUE_2 = 2;

    /**
     * 业务常量 {@code RECORD_INVOICE_VALIDATION_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int RECORD_INVOICE_VALIDATION_VALUE_3 = 3;

    /**
     * 业务常量 {@code WITHDRAW_VALUE_4}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int WITHDRAW_VALUE_4 = 4;
}
