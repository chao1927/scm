package com.chaobo.scm.supplier.application.finance;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.infrastructure.persistence.finance.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;

/**
 * SupplierFinanceLifecycleApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierFinanceLifecycleApplicationService {

    /**
     * finance（类型：{@code SupplierFinanceMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierFinanceMapper finance;

    /**
     * lifecycle（类型：{@code SupplierFinanceLifecycleMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierFinanceLifecycleMapper lifecycle;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * 创建 SupplierFinanceLifecycleApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param finance 业务处理参数或成员，类型为 {@code SupplierFinanceMapper}
     * @param lifecycle 业务处理参数或成员，类型为 {@code SupplierFinanceLifecycleMapper}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     */
    public SupplierFinanceLifecycleApplicationService(SupplierFinanceMapper finance, SupplierFinanceLifecycleMapper lifecycle, AuditLogRepository audit) {
        this.finance = finance;
        this.lifecycle = lifecycle;
        this.audit = audit;
    }

    /**
     * 处理当前类型职责中的操作 {@code resolveDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param resolution 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolveDifference(long id, int version, BigDecimal amount, String resolution, CommandContext c) {
        c.requirePermission("supplier:reconciliation:resolve");
        var row = reconciliation(id);
        c.requireSupplierScope(row.supplierId());
        validAmount(amount);
        if (resolution == null || resolution.isBlank()) {
            throw rule("差异处理结论不能为空");
        }
        if (lifecycle.resolveDifference(id, version, amount, resolution.trim()) != 1) {
            throw conflict();
        }
        audit.save(c, "RESOLVE_RECONCILIATION_DIFFERENCE", "RECONCILIATION", id, row.statementNo(), null, "{\"status\":2}");
    }

    /**
     * 执行命令 {@code closeReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeReconciliation(long id, int version, CommandContext c) {
        c.requirePermission("supplier:reconciliation:close");
        var row = reconciliation(id);
        c.requireSupplierScope(row.supplierId());
        if (lifecycle.close(id, version) != 1) {
            throw conflict();
        }
        audit.save(c, "CLOSE_RECONCILIATION", "RECONCILIATION", id, row.statementNo(), null, "{\"status\":5}");
    }

    /**
     * 执行命令 {@code closeFromBms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeFromBms(String no, long supplierId, int sourceVersion) {
        if (no == null || no.isBlank() || supplierId <= 0) {
            throw rule("BMS 对账关闭数据不合法");
        }
        lifecycle.closeFromBms(no, supplierId, sourceVersion);
    }

    /**
     * 处理当前类型职责中的操作 {@code resubmitInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param net 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param tax 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param url 业务处理参数或成员，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void resubmitInvoice(long id, int version, BigDecimal net, BigDecimal tax, BigDecimal rate, String url, CommandContext c) {
        c.requirePermission("supplier:invoice:resubmit");
        var row = invoice(id);
        c.requireSupplierScope(row.supplierId());
        validInvoice(net, tax, rate, url);
        if (lifecycle.resubmitInvoice(id, version, net, tax, rate, url) != 1) {
            throw conflict();
        }
        audit.save(c, "RESUBMIT_INVOICE", "INVOICE", id, row.invoiceNo(), null, "{\"status\":1}");
    }

    /**
     * 执行命令 {@code closeInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeInvoice(long id, int version, CommandContext c) {
        c.requirePermission("supplier:invoice:close");
        var row = invoice(id);
        c.requireSupplierScope(row.supplierId());
        if (lifecycle.closeInvoice(id, version) != 1) {
            throw conflict();
        }
        audit.save(c, "CLOSE_INVOICE", "INVOICE", id, row.invoiceNo(), null, "{\"status\":4}");
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciliation}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FinanceViews.Reconciliation}
     */
    private FinanceViews.Reconciliation reconciliation(long id) {
        var r = finance.reconciliation(id);
        if (r == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "对账单不存在");
        }
        return r;
    }

    /**
     * 处理当前类型职责中的操作 {@code invoice}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FinanceViews.Invoice}
     */
    private FinanceViews.Invoice invoice(long id) {
        var r = finance.invoice(id);
        if (r == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "发票不存在");
        }
        return r;
    }

    /**
     * 处理当前类型职责中的操作 {@code validAmount}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     */
    private static void validAmount(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw rule("金额不合法");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code validInvoice}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param net 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param tax 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param url 业务处理参数或成员，类型为 {@code String}
     */
    private static void validInvoice(BigDecimal net, BigDecimal tax, BigDecimal rate, String url) {
        validAmount(net);
        validAmount(tax);
        if (rate == null || rate.signum() < 0 || url == null || url.isBlank()) {
            throw rule("发票数据不合法");
        }
        if (net.multiply(rate).setScale(VALID_INVOICE_VALUE_2, RoundingMode.HALF_UP).compareTo(tax.setScale(VALID_INVOICE_VALUE_2, RoundingMode.HALF_UP)) != 0) {
            throw rule("税额与不含税金额、税率不匹配");
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
     * 业务常量 {@code VALID_INVOICE_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VALID_INVOICE_VALUE_2 = 2;
}
