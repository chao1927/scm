package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.domain.BmsDomain;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BmsApplicationService {
    private final BmsMapper mapper;
    private final AtomicLong objectSequence = new AtomicLong(100000);
    private final AtomicLong ruleSequence = new AtomicLong(200000);
    private final AtomicLong sourceSequence = new AtomicLong(300000);
    private final AtomicLong chargeSequence = new AtomicLong(400000);
    private final AtomicLong adjustmentSequence = new AtomicLong(500000);
    private final AtomicLong reconciliationSequence = new AtomicLong(600000);
    private final AtomicLong billSequence = new AtomicLong(700000);
    private final AtomicLong invoiceSequence = new AtomicLong(800000);
    private final AtomicLong financeSequence = new AtomicLong(900000);
    private final AtomicLong refundSequence = new AtomicLong(1000000);
    private final AtomicLong eventSequence = new AtomicLong(1100000);
    private final AtomicLong inboxSequence = new AtomicLong(1200000);

    public BmsApplicationService(BmsMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public BmsMapper.BillingObjectRow createBillingObject(CreateBillingObjectCommand command) {
        String objectCode = command.objectCode() == null || command.objectCode().isBlank()
                ? "BO" + objectSequence.incrementAndGet()
                : command.objectCode();
        BmsDomain.BillingObjectAggregate aggregate = BmsDomain.BillingObjectAggregate.create(objectCode,
                command.objectName(), command.objectType(), command.direction(), command.currency());
        BmsMapper.BillingObjectRow row = toRow(aggregate);
        mapper.insertBillingObject(row);
        outbox("BillingObjectCreated", row.objectCode(), row.objectCode(), "{}");
        log("CREATE_BILLING_OBJECT", row.objectCode(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    public List<BmsMapper.BillingObjectRow> listBillingObjects(Integer status) {
        return mapper.listBillingObjects(status);
    }

    @Transactional
    public BmsMapper.BillingObjectRow enableBillingObject(String objectCode, VersionCommand command) {
        BmsDomain.BillingObjectAggregate aggregate = loadBillingObject(objectCode);
        aggregate.enable(command.expectedVersion());
        mapper.updateBillingObject(toRow(aggregate));
        outbox("BillingObjectEnabled", objectCode, objectCode, "{}");
        log("ENABLE_BILLING_OBJECT", objectCode, command.operatorId(), command.idempotencyKey());
        return mapper.findBillingObject(objectCode);
    }

    @Transactional
    public BmsMapper.BillingObjectRow disableBillingObject(String objectCode, VersionCommand command) {
        BmsDomain.BillingObjectAggregate aggregate = loadBillingObject(objectCode);
        aggregate.disable(command.expectedVersion());
        mapper.updateBillingObject(toRow(aggregate));
        outbox("BillingObjectDisabled", objectCode, objectCode, "{}");
        log("DISABLE_BILLING_OBJECT", objectCode, command.operatorId(), command.idempotencyKey());
        return mapper.findBillingObject(objectCode);
    }

    @Transactional
    public BmsMapper.BillingRuleRow createBillingRule(CreateBillingRuleCommand command) {
        BmsDomain.BillingObjectAggregate object = loadBillingObject(command.objectCode());
        object.ensureEnabled();
        BmsDomain.BillingRuleAggregate aggregate = BmsDomain.BillingRuleAggregate.create(
                "BR" + ruleSequence.incrementAndGet(), command.objectCode(), command.feeType(),
                command.unitPrice(), command.taxRate(), command.effectiveFrom(), command.effectiveTo());
        BmsMapper.BillingRuleRow row = toRow(aggregate);
        mapper.insertBillingRule(row);
        log("CREATE_BILLING_RULE", row.ruleNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public BmsMapper.BillingRuleRow publishBillingRule(String ruleNo, VersionCommand command) {
        BmsDomain.BillingRuleAggregate aggregate = loadBillingRule(ruleNo);
        if (mapper.countPublishedRuleOverlap(aggregate.objectCode(), aggregate.feeType(),
                aggregate.effectiveFrom(), aggregate.effectiveTo()) > 0) {
            throw new IllegalStateException("published billing rule effective range overlaps");
        }
        aggregate.publish(command.expectedVersion());
        mapper.updateBillingRule(toRow(aggregate));
        outbox("BillingRulePublished", ruleNo, aggregate.objectCode(), "{\"ruleNo\":\"" + ruleNo + "\"}");
        log("PUBLISH_BILLING_RULE", ruleNo, command.operatorId(), command.idempotencyKey());
        return mapper.findBillingRule(ruleNo);
    }

    public List<BmsMapper.BillingRuleRow> listBillingRules(String objectCode) {
        return mapper.listBillingRules(objectCode);
    }

    @Transactional
    public BmsMapper.ChargeSourceRow collectChargeSource(CollectChargeSourceCommand command) {
        BmsMapper.ChargeSourceRow existing =
                mapper.findChargeSourceByIdempotency(command.sourceSystem(), command.idempotencyKey());
        if (existing != null) {
            return existing;
        }
        BmsDomain.ChargeSourceAggregate source = BmsDomain.ChargeSourceAggregate.create(
                "CS" + sourceSequence.incrementAndGet(), command.sourceSystem(), command.sourceEventId(),
                command.billingObjectCode(), command.feeType(), command.quantity());
        BmsMapper.ChargeSourceRow row = toRow(source, command.idempotencyKey(), command.billingPeriod(),
                command.payload());
        try {
            calculateSource(source, command.billingPeriod());
            row = toRow(source, command.idempotencyKey(), command.billingPeriod(), command.payload());
        } catch (RuntimeException ex) {
            source.fail(ex.getMessage());
            row = toRow(source, command.idempotencyKey(), command.billingPeriod(), command.payload());
            mapper.insertChargeSource(row);
            outbox("ChargeSourceFailed", row.sourceNo(), row.billingObjectCode(), "{\"reason\":\""
                    + sanitize(ex.getMessage()) + "\"}");
            log("COLLECT_CHARGE_SOURCE_FAILED", row.sourceNo(), command.operatorId(), command.idempotencyKey());
            return row;
        }
        mapper.insertChargeSource(row);
        BmsMapper.ChargeDetailRow detail = createChargeDetail(source, command.billingPeriod());
        mapper.insertChargeDetail(detail);
        outbox("ChargeSourceAccepted", row.sourceNo(), row.billingObjectCode(), "{}");
        outbox("ChargeCalculated", detail.chargeNo(), detail.objectCode(), "{}");
        log("COLLECT_CHARGE_SOURCE", row.sourceNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    public List<BmsMapper.ChargeSourceRow> listChargeSources(Integer status) {
        return mapper.listChargeSources(status);
    }

    @Transactional
    public BmsMapper.ChargeSourceRow replayChargeSource(String sourceNo, ReplayCommand command) {
        BmsMapper.ChargeSourceRow row = require(mapper.findChargeSource(sourceNo), "charge source not found");
        BmsDomain.ChargeSourceAggregate source = BmsDomain.ChargeSourceAggregate.restore(row.sourceNo(),
                row.sourceSystem(), row.sourceEventId(), row.billingObjectCode(), row.feeType(), row.quantity(),
                row.status(), row.failureReason(), row.version());
        source.replay();
        try {
            calculateSource(source, row.billingPeriod());
            mapper.updateChargeSource(toRow(source, row.idempotencyKey(), row.billingPeriod(), row.payload()));
            if (mapper.findChargeBySource(source.sourceNo()) == null) {
                mapper.insertChargeDetail(createChargeDetail(source, row.billingPeriod()));
            }
            outbox("ChargeSourceAccepted", source.sourceNo(), source.billingObjectCode(), "{}");
        } catch (RuntimeException ex) {
            source.fail(ex.getMessage());
            mapper.updateChargeSource(toRow(source, row.idempotencyKey(), row.billingPeriod(), row.payload()));
            outbox("ChargeSourceFailed", source.sourceNo(), source.billingObjectCode(), "{\"reason\":\""
                    + sanitize(ex.getMessage()) + "\"}");
        }
        log("REPLAY_CHARGE_SOURCE", sourceNo, command.operatorId(), command.idempotencyKey());
        return mapper.findChargeSource(sourceNo);
    }

    public List<BmsMapper.ChargeDetailRow> listCharges(String objectCode, String billingPeriod, Integer status) {
        return mapper.listCharges(objectCode, billingPeriod, status);
    }

    @Transactional
    public BmsMapper.ChargeDetailRow recalculateCharge(String chargeNo, RecalculateChargeCommand command) {
        BmsMapper.ChargeDetailRow row = require(mapper.findChargeDetail(chargeNo), "charge detail not found");
        BmsMapper.BillingRuleRow rule = require(mapper.findBillingRule(row.ruleNo()), "billing rule not found");
        BmsDomain.BillingRuleAggregate ruleAggregate = restoreRule(rule);
        BigDecimal quantity = command.quantity() == null ? row.quantity() : command.quantity();
        BmsDomain.ChargeDetailAggregate aggregate = restoreCharge(row);
        aggregate.recalculate(quantity, rule.unitPrice(), ruleAggregate.calculate(quantity), command.expectedVersion());
        mapper.updateChargeDetail(toRow(aggregate, row.billingPeriod()));
        outbox("ChargeRecalculated", chargeNo, row.objectCode(), "{}");
        log("RECALCULATE_CHARGE", chargeNo, command.operatorId(), command.idempotencyKey());
        return mapper.findChargeDetail(chargeNo);
    }

    @Transactional
    public BmsMapper.ChargeDetailRow voidCharge(String chargeNo, VersionCommand command) {
        BmsMapper.ChargeDetailRow row = require(mapper.findChargeDetail(chargeNo), "charge detail not found");
        BmsDomain.ChargeDetailAggregate aggregate = restoreCharge(row);
        aggregate.voidCharge(command.expectedVersion());
        mapper.updateChargeDetail(toRow(aggregate, row.billingPeriod()));
        outbox("ChargeVoided", chargeNo, row.objectCode(), "{}");
        log("VOID_CHARGE", chargeNo, command.operatorId(), command.idempotencyKey());
        return mapper.findChargeDetail(chargeNo);
    }

    @Transactional
    public BmsMapper.AdjustmentRow createAdjustment(CreateAdjustmentCommand command) {
        BmsMapper.ChargeDetailRow original =
                require(mapper.findChargeDetail(command.originalChargeNo()), "original charge detail not found");
        BmsDomain.ChargeAdjustmentAggregate aggregate = BmsDomain.ChargeAdjustmentAggregate.create(
                "BA" + adjustmentSequence.incrementAndGet(), original.chargeNo(), command.adjustAmount(),
                command.approved());
        BmsMapper.AdjustmentRow row = toRow(aggregate, command.reason());
        mapper.insertAdjustment(row);
        log("CREATE_CHARGE_ADJUSTMENT", row.adjustmentNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public BmsMapper.AdjustmentRow executeAdjustment(String adjustmentNo, VersionCommand command) {
        BmsMapper.AdjustmentRow row = require(mapper.findAdjustment(adjustmentNo), "adjustment not found");
        BmsDomain.ChargeAdjustmentAggregate aggregate = BmsDomain.ChargeAdjustmentAggregate.restore(
                row.adjustmentNo(), row.originalChargeNo(), row.adjustAmount(), row.status(), row.version());
        aggregate.execute(command.expectedVersion());
        mapper.updateAdjustment(toRow(aggregate, row.reason()));
        BmsMapper.ChargeDetailRow original =
                require(mapper.findChargeDetail(row.originalChargeNo()), "original charge detail not found");
        BmsMapper.ChargeDetailRow adjustmentCharge = new BmsMapper.ChargeDetailRow(null,
                "CD" + chargeSequence.incrementAndGet(), "ADJ-" + adjustmentNo, original.objectCode(),
                original.feeType(), original.ruleNo(), BigDecimal.ONE, row.adjustAmount(), row.adjustAmount(),
                BigDecimal.ZERO.setScale(2), row.adjustAmount(), original.billingPeriod(),
                BmsDomain.ChargeDetailAggregate.PENDING_RECONCILIATION, 1);
        mapper.insertChargeDetail(adjustmentCharge);
        outbox("ChargeAdjusted", adjustmentNo, original.objectCode(), "{}");
        log("EXECUTE_CHARGE_ADJUSTMENT", adjustmentNo, command.operatorId(), command.idempotencyKey());
        return mapper.findAdjustment(adjustmentNo);
    }

    @Transactional
    public BmsMapper.ReconciliationRow generateReconciliation(GenerateReconciliationCommand command) {
        List<BmsMapper.ChargeDetailRow> charges = mapper.listCharges(command.objectCode(), command.billingPeriod(),
                BmsDomain.ChargeDetailAggregate.PENDING_RECONCILIATION);
        BigDecimal total = charges.stream()
                .map(BmsMapper.ChargeDetailRow::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (charges.isEmpty() || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("no billable charges for reconciliation");
        }
        BmsDomain.ReconciliationAggregate aggregate = BmsDomain.ReconciliationAggregate.create(
                "RC" + reconciliationSequence.incrementAndGet(), command.objectCode(), command.billingPeriod(),
                total);
        BmsMapper.ReconciliationRow row = toRow(aggregate);
        mapper.insertReconciliation(row);
        outbox("ReconciliationIssued", row.reconciliationNo(), row.objectCode(), "{}");
        log("GENERATE_RECONCILIATION", row.reconciliationNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public BmsMapper.ReconciliationRow raiseReconciliationDifference(String reconciliationNo,
                                                                    DifferenceCommand command) {
        BmsMapper.ReconciliationRow row =
                require(mapper.findReconciliation(reconciliationNo), "reconciliation not found");
        BmsDomain.ReconciliationAggregate aggregate = restoreReconciliation(row);
        aggregate.raiseDifference(command.peerAmount(), command.expectedVersion());
        mapper.updateReconciliation(toRow(aggregate));
        outbox("ReconciliationDifferenceRaised", reconciliationNo, row.objectCode(), "{}");
        log("RAISE_RECONCILIATION_DIFFERENCE", reconciliationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findReconciliation(reconciliationNo);
    }

    @Transactional
    public BmsMapper.ReconciliationRow confirmReconciliation(String reconciliationNo, ConfirmAmountCommand command) {
        BmsMapper.ReconciliationRow row =
                require(mapper.findReconciliation(reconciliationNo), "reconciliation not found");
        BmsDomain.ReconciliationAggregate aggregate = restoreReconciliation(row);
        aggregate.confirm(command.confirmedAmount(), command.expectedVersion());
        mapper.updateReconciliation(toRow(aggregate));
        mapper.markChargesConfirmed(row.objectCode(), row.billingPeriod());
        outbox("ReconciliationConfirmed", reconciliationNo, row.objectCode(), "{}");
        log("CONFIRM_RECONCILIATION", reconciliationNo, command.operatorId(), command.idempotencyKey());
        return mapper.findReconciliation(reconciliationNo);
    }

    @Transactional
    public BmsMapper.BillRow generateBill(GenerateBillCommand command) {
        BmsMapper.ReconciliationRow reconciliation =
                require(mapper.findReconciliation(command.reconciliationNo()), "reconciliation not found");
        BmsDomain.ReconciliationAggregate reconciliationAggregate = restoreReconciliation(reconciliation);
        reconciliationAggregate.markBilled();
        mapper.updateReconciliation(toRow(reconciliationAggregate));
        BmsDomain.BillAggregate bill = BmsDomain.BillAggregate.create("BL" + billSequence.incrementAndGet(),
                reconciliation.reconciliationNo(), reconciliation.objectCode(), reconciliation.totalAmount());
        BmsMapper.BillRow row = toRow(bill);
        mapper.insertBill(row);
        outbox("BillGenerated", row.billNo(), row.objectCode(), "{}");
        log("GENERATE_BILL", row.billNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public BmsMapper.BillRow confirmBill(String billNo, VersionCommand command) {
        BmsMapper.BillRow row = require(mapper.findBill(billNo), "bill not found");
        BmsDomain.BillAggregate aggregate = restoreBill(row);
        aggregate.confirm(command.expectedVersion());
        mapper.updateBill(toRow(aggregate));
        outbox("BillConfirmed", billNo, row.objectCode(), "{}");
        log("CONFIRM_BILL", billNo, command.operatorId(), command.idempotencyKey());
        return mapper.findBill(billNo);
    }

    @Transactional
    public BmsMapper.InvoiceRow requestInvoice(RequestInvoiceCommand command) {
        BmsMapper.BillRow bill = require(mapper.findBill(command.billNo()), "bill not found");
        BmsDomain.InvoiceAggregate invoice = BmsDomain.InvoiceAggregate.request(
                "IV" + invoiceSequence.incrementAndGet(), bill.billNo(), command.invoiceAmount(), bill.totalAmount());
        BmsMapper.InvoiceRow row = toRow(invoice);
        mapper.insertInvoice(row);
        outbox("InvoiceRequested", row.invoiceNo(), row.billNo(), "{}");
        log("REQUEST_INVOICE", row.invoiceNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public BmsMapper.InvoiceRow issueInvoice(String invoiceNo, VersionCommand command) {
        BmsMapper.InvoiceRow row = require(mapper.findInvoice(invoiceNo), "invoice not found");
        BmsDomain.InvoiceAggregate invoice =
                BmsDomain.InvoiceAggregate.restore(row.invoiceNo(), row.billNo(), row.invoiceAmount(),
                        row.status(), row.version());
        invoice.issue(command.expectedVersion());
        mapper.updateInvoice(toRow(invoice));
        BmsMapper.BillRow billRow = require(mapper.findBill(row.billNo()), "bill not found");
        BmsDomain.BillAggregate bill = restoreBill(billRow);
        bill.markInvoiced(billRow.version());
        mapper.updateBill(toRow(bill));
        outbox("InvoiceIssued", invoiceNo, row.billNo(), "{}");
        log("ISSUE_INVOICE", invoiceNo, command.operatorId(), command.idempotencyKey());
        return mapper.findInvoice(invoiceNo);
    }

    @Transactional
    public BmsMapper.FinanceHandoverRow requestFinanceHandover(RequestFinanceCommand command) {
        BmsMapper.BillRow bill = require(mapper.findBill(command.billNo()), "bill not found");
        BmsDomain.FinanceHandoverAggregate aggregate =
                BmsDomain.FinanceHandoverAggregate.request("FH" + financeSequence.incrementAndGet(), bill.billNo());
        BmsMapper.FinanceHandoverRow row = toRow(aggregate);
        mapper.insertFinanceHandover(row);
        outbox("FinanceHandoverRequested", row.handoverNo(), row.billNo(), "{}");
        log("REQUEST_FINANCE_HANDOVER", row.handoverNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public BmsMapper.FinanceHandoverRow postFinanceHandover(String handoverNo, PostFinanceCommand command) {
        BmsMapper.FinanceHandoverRow row =
                require(mapper.findFinanceHandover(handoverNo), "finance handover not found");
        BmsDomain.FinanceHandoverAggregate aggregate = restoreFinance(row);
        aggregate.post(command.voucherNo(), command.expectedVersion());
        mapper.updateFinanceHandover(toRow(aggregate));
        BmsMapper.BillRow billRow = require(mapper.findBill(row.billNo()), "bill not found");
        BmsDomain.BillAggregate bill = restoreBill(billRow);
        bill.markPosted(billRow.version());
        mapper.updateBill(toRow(bill));
        outbox("FinancialPosted", handoverNo, row.billNo(), "{}");
        log("POST_FINANCE_HANDOVER", handoverNo, command.operatorId(), command.idempotencyKey());
        return mapper.findFinanceHandover(handoverNo);
    }

    @Transactional
    public BmsMapper.FinanceHandoverRow failFinanceHandover(String handoverNo, FailCommand command) {
        BmsMapper.FinanceHandoverRow row =
                require(mapper.findFinanceHandover(handoverNo), "finance handover not found");
        BmsDomain.FinanceHandoverAggregate aggregate = restoreFinance(row);
        aggregate.fail(command.reason(), command.expectedVersion());
        mapper.updateFinanceHandover(toRow(aggregate));
        outbox("FinancialPostFailed", handoverNo, row.billNo(), "{}");
        log("FAIL_FINANCE_HANDOVER", handoverNo, command.operatorId(), command.idempotencyKey());
        return mapper.findFinanceHandover(handoverNo);
    }

    @Transactional
    public BmsMapper.RefundSettlementRow requestRefundSettlement(RequestRefundCommand command) {
        BmsMapper.BillRow bill = require(mapper.findBill(command.billNo()), "bill not found");
        BmsDomain.RefundSettlementAggregate aggregate = BmsDomain.RefundSettlementAggregate.request(
                "RF" + refundSequence.incrementAndGet(), bill.billNo(), command.refundAmount(), bill.totalAmount());
        BmsMapper.RefundSettlementRow row = toRow(aggregate);
        mapper.insertRefundSettlement(row);
        outbox("RefundSettlementRequested", row.refundNo(), row.billNo(), "{}");
        log("REQUEST_REFUND_SETTLEMENT", row.refundNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public BmsMapper.RefundSettlementRow finishRefundSettlement(String refundNo, VersionCommand command) {
        BmsMapper.RefundSettlementRow row =
                require(mapper.findRefundSettlement(refundNo), "refund settlement not found");
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.finish(command.expectedVersion());
        mapper.updateRefundSettlement(toRow(aggregate));
        outbox("RefundSettlementFinished", refundNo, row.billNo(), "{}");
        log("FINISH_REFUND_SETTLEMENT", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    @Transactional
    public BmsMapper.RefundSettlementRow failRefundSettlement(String refundNo, FailCommand command) {
        BmsMapper.RefundSettlementRow row =
                require(mapper.findRefundSettlement(refundNo), "refund settlement not found");
        BmsDomain.RefundSettlementAggregate aggregate = restoreRefund(row);
        aggregate.fail(command.reason(), command.expectedVersion());
        mapper.updateRefundSettlement(toRow(aggregate));
        outbox("RefundSettlementFailed", refundNo, row.billNo(), "{}");
        log("FAIL_REFUND_SETTLEMENT", refundNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRefundSettlement(refundNo);
    }

    public BmsMapper.SettlementSummaryRow settlementSummary(LocalDateTime from, LocalDateTime to) {
        return mapper.settlementSummary(from, to);
    }

    @Transactional
    public BmsMapper.InboxEventRow consumeEvent(ConsumeEventCommand command) {
        BmsMapper.InboxEventRow existing = mapper.findInboxEvent(command.sourceSystem(), command.sourceEventId());
        if (existing != null) {
            return existing;
        }
        BmsMapper.InboxEventRow row = new BmsMapper.InboxEventRow("BI" + inboxSequence.incrementAndGet(),
                command.sourceSystem(), command.sourceEventId(), command.eventType(), command.businessNo(),
                command.payload(), 2, null);
        mapper.insertInboxEvent(row);
        outbox("BmsExternalEventConsumed", row.inboxNo(), row.businessNo(), "{}");
        return row;
    }

    private void calculateSource(BmsDomain.ChargeSourceAggregate source, String billingPeriod) {
        BmsDomain.BillingObjectAggregate object = loadBillingObject(source.billingObjectCode());
        object.ensureEnabled();
        BmsMapper.BillingRuleRow rule = require(mapper.findPublishedRule(source.billingObjectCode(),
                source.feeType()), "published billing rule not found");
        BmsDomain.BillingRuleAggregate ruleAggregate = restoreRule(rule);
        LocalDate date = periodStart(billingPeriod);
        if (!ruleAggregate.effectiveOn(date)) {
            throw new IllegalStateException("billing rule is not effective for period");
        }
        source.accept();
    }

    private BmsMapper.ChargeDetailRow createChargeDetail(BmsDomain.ChargeSourceAggregate source,
                                                        String billingPeriod) {
        BmsMapper.BillingRuleRow rule = require(mapper.findPublishedRule(source.billingObjectCode(),
                source.feeType()), "published billing rule not found");
        BmsDomain.BillingRuleAggregate ruleAggregate = restoreRule(rule);
        BmsDomain.ChargeDetailAggregate charge = BmsDomain.ChargeDetailAggregate.create(
                "CD" + chargeSequence.incrementAndGet(), source.sourceNo(), source.billingObjectCode(),
                source.feeType(), rule.ruleNo(), source.quantity(), rule.unitPrice(),
                ruleAggregate.calculate(source.quantity()));
        return toRow(charge, billingPeriod);
    }

    private BmsDomain.BillingObjectAggregate loadBillingObject(String objectCode) {
        BmsMapper.BillingObjectRow row = require(mapper.findBillingObject(objectCode), "billing object not found");
        return BmsDomain.BillingObjectAggregate.restore(row.objectCode(), row.objectName(), row.objectType(),
                row.direction(), row.currency(), row.status(), row.version());
    }

    private BmsDomain.BillingRuleAggregate loadBillingRule(String ruleNo) {
        return restoreRule(require(mapper.findBillingRule(ruleNo), "billing rule not found"));
    }

    private BmsDomain.BillingRuleAggregate restoreRule(BmsMapper.BillingRuleRow row) {
        return BmsDomain.BillingRuleAggregate.restore(row.ruleNo(), row.objectCode(), row.feeType(),
                row.unitPrice(), row.taxRate(), row.effectiveFrom(), row.effectiveTo(), row.status(),
                row.ruleVersion(), row.version());
    }

    private BmsDomain.ChargeDetailAggregate restoreCharge(BmsMapper.ChargeDetailRow row) {
        return BmsDomain.ChargeDetailAggregate.restore(row.chargeNo(), row.sourceNo(), row.objectCode(),
                row.feeType(), row.ruleNo(), row.quantity(), row.unitPrice(), row.amount(), row.taxAmount(),
                row.totalAmount(), row.status(), row.version());
    }

    private BmsDomain.ReconciliationAggregate restoreReconciliation(BmsMapper.ReconciliationRow row) {
        return BmsDomain.ReconciliationAggregate.restore(row.reconciliationNo(), row.objectCode(),
                row.billingPeriod(), row.totalAmount(), row.status(), row.version());
    }

    private BmsDomain.BillAggregate restoreBill(BmsMapper.BillRow row) {
        return BmsDomain.BillAggregate.restore(row.billNo(), row.reconciliationNo(), row.objectCode(),
                row.totalAmount(), row.status(), row.version());
    }

    private BmsDomain.FinanceHandoverAggregate restoreFinance(BmsMapper.FinanceHandoverRow row) {
        return BmsDomain.FinanceHandoverAggregate.restore(row.handoverNo(), row.billNo(), row.status(),
                row.voucherNo(), row.failureReason(), row.version());
    }

    private BmsDomain.RefundSettlementAggregate restoreRefund(BmsMapper.RefundSettlementRow row) {
        return BmsDomain.RefundSettlementAggregate.restore(row.refundNo(), row.billNo(), row.refundAmount(),
                row.status(), row.failureReason(), row.version());
    }

    private BmsMapper.BillingObjectRow toRow(BmsDomain.BillingObjectAggregate aggregate) {
        return new BmsMapper.BillingObjectRow(null, aggregate.objectCode(), aggregate.objectName(),
                aggregate.objectType(), aggregate.direction(), aggregate.currency(), aggregate.status(),
                aggregate.version());
    }

    private BmsMapper.BillingRuleRow toRow(BmsDomain.BillingRuleAggregate aggregate) {
        return new BmsMapper.BillingRuleRow(null, aggregate.ruleNo(), aggregate.objectCode(), aggregate.feeType(),
                aggregate.unitPrice(), aggregate.taxRate(), aggregate.effectiveFrom(), aggregate.effectiveTo(),
                aggregate.status(), aggregate.ruleVersion(), aggregate.version());
    }

    private BmsMapper.ChargeSourceRow toRow(BmsDomain.ChargeSourceAggregate aggregate, String idempotencyKey,
                                           String billingPeriod, String payload) {
        return new BmsMapper.ChargeSourceRow(null, aggregate.sourceNo(), aggregate.sourceSystem(),
                aggregate.sourceEventId(), idempotencyKey, aggregate.billingObjectCode(), aggregate.feeType(),
                aggregate.quantity(), billingPeriod, payload, aggregate.status(), aggregate.failureReason(),
                aggregate.version());
    }

    private BmsMapper.ChargeDetailRow toRow(BmsDomain.ChargeDetailAggregate aggregate, String billingPeriod) {
        return new BmsMapper.ChargeDetailRow(null, aggregate.chargeNo(), aggregate.sourceNo(), aggregate.objectCode(),
                aggregate.feeType(), aggregate.ruleNo(), aggregate.quantity(), aggregate.unitPrice(),
                aggregate.amount(), aggregate.taxAmount(), aggregate.totalAmount(), billingPeriod, aggregate.status(),
                aggregate.version());
    }

    private BmsMapper.AdjustmentRow toRow(BmsDomain.ChargeAdjustmentAggregate aggregate, String reason) {
        return new BmsMapper.AdjustmentRow(null, aggregate.adjustmentNo(), aggregate.originalChargeNo(),
                aggregate.adjustAmount(), reason, aggregate.status(), aggregate.version());
    }

    private BmsMapper.ReconciliationRow toRow(BmsDomain.ReconciliationAggregate aggregate) {
        return new BmsMapper.ReconciliationRow(null, aggregate.reconciliationNo(), aggregate.objectCode(),
                aggregate.period(), aggregate.totalAmount(), aggregate.status(), aggregate.version());
    }

    private BmsMapper.BillRow toRow(BmsDomain.BillAggregate aggregate) {
        return new BmsMapper.BillRow(null, aggregate.billNo(), aggregate.reconciliationNo(), aggregate.objectCode(),
                aggregate.totalAmount(), aggregate.status(), aggregate.version());
    }

    private BmsMapper.InvoiceRow toRow(BmsDomain.InvoiceAggregate aggregate) {
        return new BmsMapper.InvoiceRow(null, aggregate.invoiceNo(), aggregate.billNo(), aggregate.invoiceAmount(),
                aggregate.status(), aggregate.version());
    }

    private BmsMapper.FinanceHandoverRow toRow(BmsDomain.FinanceHandoverAggregate aggregate) {
        return new BmsMapper.FinanceHandoverRow(null, aggregate.handoverNo(), aggregate.billNo(), aggregate.status(),
                aggregate.voucherNo(), aggregate.failureReason(), aggregate.version());
    }

    private BmsMapper.RefundSettlementRow toRow(BmsDomain.RefundSettlementAggregate aggregate) {
        return new BmsMapper.RefundSettlementRow(null, aggregate.refundNo(), aggregate.billNo(),
                aggregate.refundAmount(), aggregate.status(), aggregate.failureReason(), aggregate.version());
    }

    private void outbox(String eventType, String aggregateNo, String businessNo, String payload) {
        mapper.insertOutboxEvent(new BmsMapper.OutboxEventRow("BE" + eventSequence.incrementAndGet(), eventType,
                aggregateNo, businessNo, payload, 1));
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new BmsMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey, LocalDateTime.now()));
    }

    private LocalDate periodStart(String billingPeriod) {
        if (billingPeriod == null || !billingPeriod.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("billing period must be yyyy-MM");
        }
        return LocalDate.parse(billingPeriod + "-01");
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }

    private <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public record CreateBillingObjectCommand(String objectCode, String objectName, String objectType, String direction,
                                             String currency, Long operatorId, String idempotencyKey) {}

    public record CreateBillingRuleCommand(String objectCode, String feeType, BigDecimal unitPrice,
                                           BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo,
                                           Long operatorId, String idempotencyKey) {}

    public record CollectChargeSourceCommand(String sourceSystem, String sourceEventId, String idempotencyKey,
                                             String billingObjectCode, String feeType, BigDecimal quantity,
                                             String billingPeriod, String payload, Long operatorId) {}

    public record RecalculateChargeCommand(BigDecimal quantity, String reason, long expectedVersion, Long operatorId,
                                           String idempotencyKey) {}

    public record CreateAdjustmentCommand(String originalChargeNo, BigDecimal adjustAmount, String reason,
                                          boolean approved, Long operatorId, String idempotencyKey) {}

    public record GenerateReconciliationCommand(String objectCode, String billingPeriod, Long operatorId,
                                                String idempotencyKey) {}

    public record DifferenceCommand(BigDecimal peerAmount, String reason, long expectedVersion, Long operatorId,
                                    String idempotencyKey) {}

    public record ConfirmAmountCommand(BigDecimal confirmedAmount, long expectedVersion, Long operatorId,
                                       String idempotencyKey) {}

    public record GenerateBillCommand(String reconciliationNo, Long operatorId, String idempotencyKey) {}

    public record RequestInvoiceCommand(String billNo, BigDecimal invoiceAmount, Long operatorId,
                                        String idempotencyKey) {}

    public record RequestFinanceCommand(String billNo, Long operatorId, String idempotencyKey) {}

    public record PostFinanceCommand(String voucherNo, long expectedVersion, Long operatorId, String idempotencyKey) {}

    public record RequestRefundCommand(String billNo, BigDecimal refundAmount, Long operatorId,
                                       String idempotencyKey) {}

    public record ConsumeEventCommand(String sourceSystem, String sourceEventId, String eventType, String businessNo,
                                      String payload) {}

    public record VersionCommand(long expectedVersion, Long operatorId, String idempotencyKey) {}

    public record ReplayCommand(Long operatorId, String idempotencyKey) {}

    public record FailCommand(String reason, long expectedVersion, Long operatorId, String idempotencyKey) {}
}
