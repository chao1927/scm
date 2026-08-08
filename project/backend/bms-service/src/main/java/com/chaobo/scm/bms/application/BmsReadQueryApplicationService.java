package com.chaobo.scm.bms.application;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.api.PageResult;
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

    public PageResult<BmsReadQueryMapper.ChargeView> charges(
        String objectCode, String billingPeriod, int pageNo, int pageSize,
        ScmAccessContext access) {
        requireRequestedScope(objectCode, access);
        return page(filter(mapper.listCharges(blankToNull(objectCode),
            blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.ChargeView::objectCode), pageNo, pageSize);
    }

    public PageResult<BmsReadQueryMapper.RuleView> rules(
        String objectCode, int pageNo, int pageSize, ScmAccessContext access) {
        requireRequestedScope(objectCode, access);
        return page(filter(mapper.listRules(blankToNull(objectCode)),
            access, BmsReadQueryMapper.RuleView::objectCode), pageNo, pageSize);
    }

    public PageResult<BmsReadQueryMapper.ReconciliationView> reconciliations(
        String billingPeriod, int pageNo, int pageSize, ScmAccessContext access) {
        return page(filter(mapper.listReconciliations(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.ReconciliationView::objectCode), pageNo, pageSize);
    }

    public PageResult<BmsReadQueryMapper.BillView> bills(
        String billingPeriod, int pageNo, int pageSize, ScmAccessContext access) {
        return page(filter(mapper.listBills(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.BillView::objectCode), pageNo, pageSize);
    }

    public PageResult<BmsReadQueryMapper.InvoiceView> invoices(
        String billingPeriod, int pageNo, int pageSize, ScmAccessContext access) {
        return page(filter(mapper.listInvoices(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.InvoiceView::objectCode), pageNo, pageSize);
    }

    public PageResult<BmsReadQueryMapper.FinanceView> finance(
        int pageNo, int pageSize, ScmAccessContext access) {
        return page(filter(mapper.listFinanceHandovers(), access,
            BmsReadQueryMapper.FinanceView::objectCode), pageNo, pageSize);
    }

    public PageResult<BmsReadQueryMapper.RefundView> refunds(
        int pageNo, int pageSize, ScmAccessContext access) {
        return page(filter(mapper.listRefunds(), access,
            BmsReadQueryMapper.RefundView::objectCode), pageNo, pageSize);
    }

    public PageResult<BmsReadQueryMapper.SettlementView> settlement(
        String billingPeriod, int pageNo, int pageSize, ScmAccessContext access) {
        return page(filter(mapper.listSettlementSummaries(blankToNull(billingPeriod)), access,
            BmsReadQueryMapper.SettlementView::objectCode), pageNo, pageSize);
    }

    /** 查询最近的结算业务操作日志。 */
    public PageResult<BmsReadQueryMapper.OperationLogView> operationLogs(
        int pageNo, int pageSize) {
        int limit = Math.max(1, Math.min(pageNo * pageSize, 200));
        return page(mapper.listOperationLogs(limit), pageNo, pageSize);
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

    private <T> PageResult<T> page(List<T> rows, int requestedPageNo,
                                   int requestedPageSize) {
        int pageNo = Math.max(1, requestedPageNo);
        int pageSize = Math.max(1, Math.min(requestedPageSize, 100));
        int fromIndex = Math.min(rows.size(), (pageNo - 1) * pageSize);
        int toIndex = Math.min(rows.size(), fromIndex + pageSize);
        return new PageResult<>(pageNo, pageSize, rows.size(),
            rows.subList(fromIndex, toIndex));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
