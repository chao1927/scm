package com.chaobo.scm.bms.application;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.bms.infrastructure.persistence.BmsReadQueryMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * BMS 八类标准页面查询服务。
 *
 * <p>读模型只返回当前用户被授予的 {@code BILLING_OBJECT} 结算对象。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class BmsReadQueryApplicationService {

    private static final String BILLING_OBJECT_SCOPE = "BILLING_OBJECT";
    private static final String WILDCARD = "*";
    private final BmsReadQueryMapper mapper;

    /**
     * 创建 BMS 查询服务。
     *
     * @param mapper BMS 页面读模型 Mapper
     */
    public BmsReadQueryApplicationService(BmsReadQueryMapper mapper) {
        this.mapper = mapper;
    }

    public List<BmsReadQueryMapper.ChargeView> charges(
        String objectCode, String billingPeriod, ScmAccessContext access) {
        requireRequestedScope(objectCode, access);
        return filter(mapper.listCharges(blankToNull(objectCode),
            blankToNull(billingPeriod)), access, BmsReadQueryMapper.ChargeView::objectCode);
    }

    public List<BmsReadQueryMapper.RuleView> rules(
        String objectCode, ScmAccessContext access) {
        requireRequestedScope(objectCode, access);
        return filter(mapper.listRules(blankToNull(objectCode)),
            access, BmsReadQueryMapper.RuleView::objectCode);
    }

    public List<BmsReadQueryMapper.ReconciliationView> reconciliations(
        String billingPeriod, ScmAccessContext access) {
        return filter(mapper.listReconciliations(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.ReconciliationView::objectCode);
    }

    public List<BmsReadQueryMapper.BillView> bills(
        String billingPeriod, ScmAccessContext access) {
        return filter(mapper.listBills(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.BillView::objectCode);
    }

    public List<BmsReadQueryMapper.InvoiceView> invoices(
        String billingPeriod, ScmAccessContext access) {
        return filter(mapper.listInvoices(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.InvoiceView::objectCode);
    }

    public List<BmsReadQueryMapper.FinanceView> finance(ScmAccessContext access) {
        return filter(mapper.listFinanceHandovers(), access,
            BmsReadQueryMapper.FinanceView::objectCode);
    }

    public List<BmsReadQueryMapper.RefundView> refunds(ScmAccessContext access) {
        return filter(mapper.listRefunds(), access,
            BmsReadQueryMapper.RefundView::objectCode);
    }

    public List<BmsReadQueryMapper.SettlementView> settlement(
        String billingPeriod, ScmAccessContext access) {
        return filter(mapper.listSettlementSummaries(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.SettlementView::objectCode);
    }

    private void requireRequestedScope(String objectCode, ScmAccessContext access) {
        if (objectCode != null && !objectCode.isBlank()) {
            access.requireScope(BILLING_OBJECT_SCOPE, objectCode.trim());
        }
    }

    private <T> List<T> filter(List<T> rows, ScmAccessContext access,
                               Function<T, String> objectCode) {
        Set<String> allowed = access.dataScopes()
            .getOrDefault(BILLING_OBJECT_SCOPE, Set.of());
        if (allowed.contains(WILDCARD)) {
            return rows;
        }
        return rows.stream().filter(row -> allowed.contains(objectCode.apply(row))).toList();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
