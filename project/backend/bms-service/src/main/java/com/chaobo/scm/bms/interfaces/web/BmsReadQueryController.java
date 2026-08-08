package com.chaobo.scm.bms.interfaces.web;

import com.chaobo.scm.bms.application.BmsReadQueryApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsReadQueryMapper;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.common.api.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * BMS 八类标准页面查询接口。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/bms/v1")
@PreAuthorize("hasAnyAuthority('*','bms:*','bms:query:read')")
public class BmsReadQueryController {

    private final BmsReadQueryApplicationService service;

    /**
     * 创建 BMS 标准页面查询接口。
     *
     * @param service 查询服务
     */
    public BmsReadQueryController(BmsReadQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/charge-details")
    public PageResult<BmsReadQueryMapper.ChargeView> charges(
        @RequestParam(required = false) String objectCode,
        @RequestParam(required = false) String billingPeriod,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.charges(objectCode, billingPeriod, pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/billing-rule-views")
    public PageResult<BmsReadQueryMapper.RuleView> rules(
        @RequestParam(required = false) String objectCode,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.rules(objectCode, pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/reconciliations")
    public PageResult<BmsReadQueryMapper.ReconciliationView> reconciliations(
        @RequestParam(required = false) String billingPeriod,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.reconciliations(billingPeriod, pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/bill-views")
    public PageResult<BmsReadQueryMapper.BillView> bills(
        @RequestParam(required = false) String billingPeriod,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.bills(billingPeriod, pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/invoice-views")
    public PageResult<BmsReadQueryMapper.InvoiceView> invoices(
        @RequestParam(required = false) String billingPeriod,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.invoices(billingPeriod, pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/finance-handoff-views")
    public PageResult<BmsReadQueryMapper.FinanceView> finance(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.finance(pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/refund-views")
    public PageResult<BmsReadQueryMapper.RefundView> refunds(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.refunds(pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/settlement-report-views")
    public PageResult<BmsReadQueryMapper.SettlementView> settlement(
        @RequestParam(required = false) String billingPeriod,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        Authentication authentication) {
        return service.settlement(billingPeriod, pageNo, pageSize,
            ScmAccessContexts.require(authentication));
    }

    /** 查询结算业务操作审计日志。 */
    @GetMapping("/operation-logs")
    public PageResult<BmsReadQueryMapper.OperationLogView> operationLogs(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize) {
        return service.operationLogs(pageNo, pageSize);
    }
}
