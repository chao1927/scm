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

/**
 * BmsController。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'bms:*', 'bms:finance:manage')")
public class BmsController {

    /**
     * service（类型：{@code BmsApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final BmsApplicationService service;

    /**
     * 创建 BmsController。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param service 应用或外部协作依赖，类型为 {@code BmsApplicationService}
     */
    public BmsController(BmsApplicationService service) {
        this.service = service;
    }

    /**
     * 执行命令 {@code createBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.CreateBillingObjectCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingObjectRow}
     */
    @PostMapping("/api/bms/v1/billing-subjects")
    public BmsMapper.BillingObjectRow createBillingObject(@RequestBody BmsApplicationService.CreateBillingObjectCommand command) {
        return service.createBillingObject(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code billingObjects}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<BmsMapper.BillingObjectRow>}
     */
    @GetMapping("/api/bms/v1/billing-subjects")
    public List<BmsMapper.BillingObjectRow> billingObjects(@RequestParam(required = false) Integer status) {
        return service.listBillingObjects(status);
    }

    /**
     * 执行命令 {@code enableBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingObjectRow}
     */
    @PostMapping("/api/bms/v1/billing-subjects/{objectCode}/enable")
    public BmsMapper.BillingObjectRow enableBillingObject(@PathVariable String objectCode, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.enableBillingObject(objectCode, command);
    }

    /**
     * 执行命令 {@code disableBillingObject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingObjectRow}
     */
    @PostMapping("/api/bms/v1/billing-subjects/{objectCode}/disable")
    public BmsMapper.BillingObjectRow disableBillingObject(@PathVariable String objectCode, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.disableBillingObject(objectCode, command);
    }

    /**
     * 执行命令 {@code createBillingRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.CreateBillingRuleCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingRuleRow}
     */
    @PostMapping("/api/bms/v1/billing-rules")
    public BmsMapper.BillingRuleRow createBillingRule(@RequestBody BmsApplicationService.CreateBillingRuleCommand command) {
        return service.createBillingRule(command);
    }

    /**
     * 执行命令 {@code publishBillingRule}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillingRuleRow}
     */
    @PostMapping("/api/bms/v1/billing-rules/{ruleNo}/publish")
    public BmsMapper.BillingRuleRow publishBillingRule(@PathVariable String ruleNo, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.publishBillingRule(ruleNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code billingRules}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<BmsMapper.BillingRuleRow>}
     */
    @GetMapping("/api/bms/v1/billing-rules")
    public List<BmsMapper.BillingRuleRow> billingRules(@RequestParam String objectCode) {
        return service.listBillingRules(objectCode);
    }

    /**
     * 处理当前类型职责中的操作 {@code chargeSources}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<BmsMapper.ChargeSourceRow>}
     */
    @GetMapping("/api/bms/v1/charge-sources")
    public List<BmsMapper.ChargeSourceRow> chargeSources(@RequestParam(required = false) Integer status) {
        return service.listChargeSources(status);
    }

    /**
     * 执行命令 {@code replayChargeSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.ReplayCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.ChargeSourceRow}
     */
    @PostMapping("/api/bms/v1/charge-sources/{sourceNo}/replay")
    public BmsMapper.ChargeSourceRow replayChargeSource(@PathVariable String sourceNo, @RequestBody BmsApplicationService.ReplayCommand command) {
        return service.replayChargeSource(sourceNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code charges}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param objectCode 可追踪业务编码，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<BmsMapper.ChargeDetailRow>}
     */
    @GetMapping("/api/bms/v1/charges")
    public List<BmsMapper.ChargeDetailRow> charges(@RequestParam String objectCode, @RequestParam String billingPeriod, @RequestParam(required = false) Integer status) {
        return service.listCharges(objectCode, billingPeriod, status);
    }

    /**
     * 处理当前类型职责中的操作 {@code recalculateCharge}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param chargeNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.RecalculateChargeCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ChargeDetailRow}
     */
    @PostMapping("/api/bms/v1/charges/{chargeNo}/recalculate")
    public BmsMapper.ChargeDetailRow recalculateCharge(@PathVariable String chargeNo, @RequestBody BmsApplicationService.RecalculateChargeCommand command) {
        return service.recalculateCharge(chargeNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code voidCharge}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param chargeNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ChargeDetailRow}
     */
    @PostMapping("/api/bms/v1/charges/{chargeNo}/void")
    public BmsMapper.ChargeDetailRow voidCharge(@PathVariable String chargeNo, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.voidCharge(chargeNo, command);
    }

    /**
     * 执行命令 {@code createAdjustment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.CreateAdjustmentCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.AdjustmentRow}
     */
    @PostMapping("/api/bms/v1/charge-adjustments")
    public BmsMapper.AdjustmentRow createAdjustment(@RequestBody BmsApplicationService.CreateAdjustmentCommand command) {
        return service.createAdjustment(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code executeAdjustment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param adjustmentNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.AdjustmentRow}
     */
    @PostMapping("/api/bms/v1/charge-adjustments/{adjustmentNo}/execute")
    public BmsMapper.AdjustmentRow executeAdjustment(@PathVariable String adjustmentNo, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.executeAdjustment(adjustmentNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code generateReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.GenerateReconciliationCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ReconciliationRow}
     */
    @PostMapping("/api/bms/v1/reconciliation-statements")
    public BmsMapper.ReconciliationRow generateReconciliation(@RequestBody BmsApplicationService.GenerateReconciliationCommand command) {
        return service.generateReconciliation(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code raiseReconciliationDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.DifferenceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.ReconciliationRow}
     */
    @PostMapping("/api/bms/v1/reconciliation-statements/{reconciliationNo}/difference")
    public BmsMapper.ReconciliationRow raiseReconciliationDifference(@PathVariable String reconciliationNo, @RequestBody BmsApplicationService.DifferenceCommand command) {
        return service.raiseReconciliationDifference(reconciliationNo, command);
    }

    /**
     * 执行命令 {@code confirmReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.ConfirmAmountCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.ReconciliationRow}
     */
    @PostMapping("/api/bms/v1/reconciliation-statements/{reconciliationNo}/confirm")
    public BmsMapper.ReconciliationRow confirmReconciliation(@PathVariable String reconciliationNo, @RequestBody BmsApplicationService.ConfirmAmountCommand command) {
        return service.confirmReconciliation(reconciliationNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code generateBill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.GenerateBillCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.BillRow}
     */
    @PostMapping("/api/bms/v1/bills")
    public BmsMapper.BillRow generateBill(@RequestBody BmsApplicationService.GenerateBillCommand command) {
        return service.generateBill(command);
    }

    /**
     * 执行命令 {@code confirmBill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param billNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.BillRow}
     */
    @PostMapping("/api/bms/v1/bills/{billNo}/confirm")
    public BmsMapper.BillRow confirmBill(@PathVariable String billNo, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.confirmBill(billNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.RequestInvoiceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.InvoiceRow}
     */
    @PostMapping("/api/bms/v1/invoices")
    public BmsMapper.InvoiceRow requestInvoice(@RequestBody BmsApplicationService.RequestInvoiceCommand command) {
        return service.requestInvoice(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code issueInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param invoiceNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.InvoiceRow}
     */
    @PostMapping("/api/bms/v1/invoices/{invoiceNo}/issue")
    public BmsMapper.InvoiceRow issueInvoice(@PathVariable String invoiceNo, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.issueInvoice(invoiceNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestFinance}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.RequestFinanceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.FinanceHandoverRow}
     */
    @PostMapping("/api/bms/v1/financial-handovers")
    public BmsMapper.FinanceHandoverRow requestFinance(@RequestBody BmsApplicationService.RequestFinanceCommand command) {
        return service.requestFinanceHandover(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code postFinance}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param handoverNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.PostFinanceCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.FinanceHandoverRow}
     */
    @PostMapping("/api/bms/v1/financial-handovers/{handoverNo}/post")
    public BmsMapper.FinanceHandoverRow postFinance(@PathVariable String handoverNo, @RequestBody BmsApplicationService.PostFinanceCommand command) {
        return service.postFinanceHandover(handoverNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code failFinance}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param handoverNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.FailCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.FinanceHandoverRow}
     */
    @PostMapping("/api/bms/v1/financial-handovers/{handoverNo}/fail")
    public BmsMapper.FinanceHandoverRow failFinance(@PathVariable String handoverNo, @RequestBody BmsApplicationService.FailCommand command) {
        return service.failFinanceHandover(handoverNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.RequestRefundCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @PostMapping("/api/bms/v1/refund-settlements")
    public BmsMapper.RefundSettlementRow requestRefund(@RequestBody BmsApplicationService.RequestRefundCommand command) {
        return service.requestRefundSettlement(command);
    }

    /**
     * 处理当前类型职责中的操作 {@code finishRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @PostMapping("/api/bms/v1/refund-settlements/{refundNo}/finish")
    public BmsMapper.RefundSettlementRow finishRefund(@PathVariable String refundNo, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.finishRefundSettlement(refundNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code failRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.FailCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @PostMapping("/api/bms/v1/refund-settlements/{refundNo}/fail")
    public BmsMapper.RefundSettlementRow failRefund(@PathVariable String refundNo, @RequestBody BmsApplicationService.FailCommand command) {
        return service.failRefundSettlement(refundNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code refundReceipt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.RefundReceiptCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @PostMapping("/openapi/bms/v1/refund-settlements/{refundNo}/receipts")
    public BmsMapper.RefundSettlementRow refundReceipt(@PathVariable String refundNo, @RequestBody BmsApplicationService.RefundReceiptCommand command) {
        return service.consumeRefundReceipt(refundNo, command);
    }

    /**
     * 执行命令 {@code retryRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param refundNo 可追踪业务编码，类型为 {@code String}
     * @param command 用例输入命令，类型为 {@code BmsApplicationService.VersionCommand}
     * @return 执行命令的结果，类型为 {@code BmsMapper.RefundSettlementRow}
     */
    @PostMapping("/api/bms/v1/refund-settlements/{refundNo}/retry")
    public BmsMapper.RefundSettlementRow retryRefund(@PathVariable String refundNo, @RequestBody BmsApplicationService.VersionCommand command) {
        return service.retryRefundSettlement(refundNo, command);
    }

    /** 将支付结果未知的退款转入待确认，并保留额度占用。 */
    @PostMapping("/internal/bms/v1/refund-settlements/{refundNo}/confirmation-pending")
    public BmsMapper.RefundSettlementRow markRefundConfirmationPending(
            @PathVariable String refundNo,
            @RequestBody BmsApplicationService.ConfirmationPendingCommand command) {
        return service.markRefundConfirmationPending(refundNo, command);
    }

    /** 高风险人工关闭，由应用服务校验凭证和双人复核。 */
    @PostMapping("/api/bms/v1/refund-settlements/{refundNo}/manual-close")
    @org.springframework.security.access.prepost.PreAuthorize(
        "hasAnyAuthority('*','bms:*','bms:refund:manual-resolve')")
    public BmsMapper.RefundSettlementRow closeRefundManually(
            @PathVariable String refundNo,
            @RequestBody BmsApplicationService.ManualRefundResolutionCommand command) {
        return service.closeRefundManually(refundNo, command);
    }

    /** 高风险人工确认退款完成。 */
    @PostMapping("/api/bms/v1/refund-settlements/{refundNo}/manual-complete")
    @org.springframework.security.access.prepost.PreAuthorize(
        "hasAnyAuthority('*','bms:*','bms:refund:manual-resolve')")
    public BmsMapper.RefundSettlementRow completeRefundManually(
            @PathVariable String refundNo,
            @RequestBody BmsApplicationService.ManualRefundResolutionCommand command) {
        return service.completeRefundManually(refundNo, command);
    }

    /**
     * 处理当前类型职责中的操作 {@code settlementSummary}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param from 业务处理参数或成员，类型为 {@code LocalDateTime}
     * @param to 业务处理参数或成员，类型为 {@code LocalDateTime}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BmsMapper.SettlementSummaryRow}
     */
    @GetMapping("/api/bms/v1/reports/settlement-summary")
    public BmsMapper.SettlementSummaryRow settlementSummary(@RequestParam LocalDateTime from, @RequestParam LocalDateTime to) {
        return service.settlementSummary(from, to);
    }

}
