package com.chaobo.scm.bms.interfaces.web;

import com.chaobo.scm.bms.application.BmsApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
public class BmsController {
    private final BmsApplicationService service;

    public BmsController(BmsApplicationService service) {
        this.service = service;
    }

    @PostMapping("/api/bms/v1/billing-subjects")
    public BmsMapper.BillingObjectRow createBillingObject(
            @RequestBody BmsApplicationService.CreateBillingObjectCommand command) {
        return service.createBillingObject(command);
    }

    @GetMapping("/api/bms/v1/billing-subjects")
    public List<BmsMapper.BillingObjectRow> billingObjects(@RequestParam(required = false) Integer status) {
        return service.listBillingObjects(status);
    }

    @PostMapping("/api/bms/v1/billing-subjects/{objectCode}/enable")
    public BmsMapper.BillingObjectRow enableBillingObject(
            @PathVariable String objectCode,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.enableBillingObject(objectCode, command);
    }

    @PostMapping("/api/bms/v1/billing-subjects/{objectCode}/disable")
    public BmsMapper.BillingObjectRow disableBillingObject(
            @PathVariable String objectCode,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.disableBillingObject(objectCode, command);
    }

    @PostMapping("/api/bms/v1/billing-rules")
    public BmsMapper.BillingRuleRow createBillingRule(
            @RequestBody BmsApplicationService.CreateBillingRuleCommand command) {
        return service.createBillingRule(command);
    }

    @PostMapping("/api/bms/v1/billing-rules/{ruleNo}/publish")
    public BmsMapper.BillingRuleRow publishBillingRule(
            @PathVariable String ruleNo,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.publishBillingRule(ruleNo, command);
    }

    @GetMapping("/api/bms/v1/billing-rules")
    public List<BmsMapper.BillingRuleRow> billingRules(@RequestParam String objectCode) {
        return service.listBillingRules(objectCode);
    }

    @PostMapping("/openapi/bms/v1/charge-sources")
    public BmsMapper.ChargeSourceRow collectChargeSource(
            @RequestBody BmsApplicationService.CollectChargeSourceCommand command) {
        return service.collectChargeSource(command);
    }

    @GetMapping("/api/bms/v1/charge-sources")
    public List<BmsMapper.ChargeSourceRow> chargeSources(@RequestParam(required = false) Integer status) {
        return service.listChargeSources(status);
    }

    @PostMapping("/api/bms/v1/charge-sources/{sourceNo}/replay")
    public BmsMapper.ChargeSourceRow replayChargeSource(
            @PathVariable String sourceNo,
            @RequestBody BmsApplicationService.ReplayCommand command) {
        return service.replayChargeSource(sourceNo, command);
    }

    @GetMapping("/api/bms/v1/charges")
    public List<BmsMapper.ChargeDetailRow> charges(@RequestParam String objectCode,
                                                   @RequestParam String billingPeriod,
                                                   @RequestParam(required = false) Integer status) {
        return service.listCharges(objectCode, billingPeriod, status);
    }

    @PostMapping("/api/bms/v1/charges/{chargeNo}/recalculate")
    public BmsMapper.ChargeDetailRow recalculateCharge(
            @PathVariable String chargeNo,
            @RequestBody BmsApplicationService.RecalculateChargeCommand command) {
        return service.recalculateCharge(chargeNo, command);
    }

    @PostMapping("/api/bms/v1/charges/{chargeNo}/void")
    public BmsMapper.ChargeDetailRow voidCharge(
            @PathVariable String chargeNo,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.voidCharge(chargeNo, command);
    }

    @PostMapping("/api/bms/v1/charge-adjustments")
    public BmsMapper.AdjustmentRow createAdjustment(
            @RequestBody BmsApplicationService.CreateAdjustmentCommand command) {
        return service.createAdjustment(command);
    }

    @PostMapping("/api/bms/v1/charge-adjustments/{adjustmentNo}/execute")
    public BmsMapper.AdjustmentRow executeAdjustment(
            @PathVariable String adjustmentNo,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.executeAdjustment(adjustmentNo, command);
    }

    @PostMapping("/api/bms/v1/reconciliation-statements")
    public BmsMapper.ReconciliationRow generateReconciliation(
            @RequestBody BmsApplicationService.GenerateReconciliationCommand command) {
        return service.generateReconciliation(command);
    }

    @PostMapping("/api/bms/v1/reconciliation-statements/{reconciliationNo}/difference")
    public BmsMapper.ReconciliationRow raiseReconciliationDifference(
            @PathVariable String reconciliationNo,
            @RequestBody BmsApplicationService.DifferenceCommand command) {
        return service.raiseReconciliationDifference(reconciliationNo, command);
    }

    @PostMapping("/api/bms/v1/reconciliation-statements/{reconciliationNo}/confirm")
    public BmsMapper.ReconciliationRow confirmReconciliation(
            @PathVariable String reconciliationNo,
            @RequestBody BmsApplicationService.ConfirmAmountCommand command) {
        return service.confirmReconciliation(reconciliationNo, command);
    }

    @PostMapping("/api/bms/v1/bills")
    public BmsMapper.BillRow generateBill(@RequestBody BmsApplicationService.GenerateBillCommand command) {
        return service.generateBill(command);
    }

    @PostMapping("/api/bms/v1/bills/{billNo}/confirm")
    public BmsMapper.BillRow confirmBill(
            @PathVariable String billNo,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.confirmBill(billNo, command);
    }

    @PostMapping("/api/bms/v1/invoices")
    public BmsMapper.InvoiceRow requestInvoice(@RequestBody BmsApplicationService.RequestInvoiceCommand command) {
        return service.requestInvoice(command);
    }

    @PostMapping("/api/bms/v1/invoices/{invoiceNo}/issue")
    public BmsMapper.InvoiceRow issueInvoice(
            @PathVariable String invoiceNo,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.issueInvoice(invoiceNo, command);
    }

    @PostMapping("/api/bms/v1/financial-handovers")
    public BmsMapper.FinanceHandoverRow requestFinance(
            @RequestBody BmsApplicationService.RequestFinanceCommand command) {
        return service.requestFinanceHandover(command);
    }

    @PostMapping("/api/bms/v1/financial-handovers/{handoverNo}/post")
    public BmsMapper.FinanceHandoverRow postFinance(
            @PathVariable String handoverNo,
            @RequestBody BmsApplicationService.PostFinanceCommand command) {
        return service.postFinanceHandover(handoverNo, command);
    }

    @PostMapping("/api/bms/v1/financial-handovers/{handoverNo}/fail")
    public BmsMapper.FinanceHandoverRow failFinance(
            @PathVariable String handoverNo,
            @RequestBody BmsApplicationService.FailCommand command) {
        return service.failFinanceHandover(handoverNo, command);
    }

    @PostMapping("/api/bms/v1/refund-settlements")
    public BmsMapper.RefundSettlementRow requestRefund(
            @RequestBody BmsApplicationService.RequestRefundCommand command) {
        return service.requestRefundSettlement(command);
    }

    @PostMapping("/api/bms/v1/refund-settlements/{refundNo}/finish")
    public BmsMapper.RefundSettlementRow finishRefund(
            @PathVariable String refundNo,
            @RequestBody BmsApplicationService.VersionCommand command) {
        return service.finishRefundSettlement(refundNo, command);
    }

    @PostMapping("/api/bms/v1/refund-settlements/{refundNo}/fail")
    public BmsMapper.RefundSettlementRow failRefund(
            @PathVariable String refundNo,
            @RequestBody BmsApplicationService.FailCommand command) {
        return service.failRefundSettlement(refundNo, command);
    }

    @GetMapping("/api/bms/v1/reports/settlement-summary")
    public BmsMapper.SettlementSummaryRow settlementSummary(@RequestParam LocalDateTime from,
                                                           @RequestParam LocalDateTime to) {
        return service.settlementSummary(from, to);
    }

    @PostMapping("/internal/bms/v1/events")
    public BmsMapper.InboxEventRow consumeEvent(
            @RequestBody BmsApplicationService.ConsumeEventCommand command) {
        return service.consumeEvent(command);
    }
}
