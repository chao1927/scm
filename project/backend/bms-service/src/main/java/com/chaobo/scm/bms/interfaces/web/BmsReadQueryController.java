package com.chaobo.scm.bms.interfaces.web;

import com.chaobo.scm.bms.application.BmsReadQueryApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsReadQueryMapper;
import com.chaobo.scm.common.security.ScmAccessContexts;
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
    public List<BmsReadQueryMapper.ChargeView> charges(
        @RequestParam(required = false) String objectCode,
        @RequestParam(required = false) String billingPeriod,
        Authentication authentication) {
        return service.charges(objectCode, billingPeriod,
            ScmAccessContexts.require(authentication));
    }

    @GetMapping("/billing-rule-views")
    public List<BmsReadQueryMapper.RuleView> rules(
        @RequestParam(required = false) String objectCode,
        Authentication authentication) {
        return service.rules(objectCode, ScmAccessContexts.require(authentication));
    }

    @GetMapping("/reconciliations")
    public List<BmsReadQueryMapper.ReconciliationView> reconciliations(
        @RequestParam(required = false) String billingPeriod,
        Authentication authentication) {
        return service.reconciliations(
            billingPeriod, ScmAccessContexts.require(authentication));
    }

    @GetMapping("/bill-views")
    public List<BmsReadQueryMapper.BillView> bills(
        @RequestParam(required = false) String billingPeriod,
        Authentication authentication) {
        return service.bills(billingPeriod, ScmAccessContexts.require(authentication));
    }

    @GetMapping("/invoice-views")
    public List<BmsReadQueryMapper.InvoiceView> invoices(
        @RequestParam(required = false) String billingPeriod,
        Authentication authentication) {
        return service.invoices(
            billingPeriod, ScmAccessContexts.require(authentication));
    }

    @GetMapping("/finance-handoff-views")
    public List<BmsReadQueryMapper.FinanceView> finance(Authentication authentication) {
        return service.finance(ScmAccessContexts.require(authentication));
    }

    @GetMapping("/refund-views")
    public List<BmsReadQueryMapper.RefundView> refunds(Authentication authentication) {
        return service.refunds(ScmAccessContexts.require(authentication));
    }

    @GetMapping("/settlement-report-views")
    public List<BmsReadQueryMapper.SettlementView> settlement(
        @RequestParam(required = false) String billingPeriod,
        Authentication authentication) {
        return service.settlement(
            billingPeriod, ScmAccessContexts.require(authentication));
    }
}
