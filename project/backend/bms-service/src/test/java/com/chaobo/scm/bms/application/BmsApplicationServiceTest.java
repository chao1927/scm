package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.domain.BmsDomain;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BmsApplicationServiceTest {
    @Test
    void completesBillingSettlementFinanceAndRefundLoop() {
        MemoryBmsMapper mapper = new MemoryBmsMapper();
        BmsApplicationService service = new BmsApplicationService(mapper);

        BmsMapper.BillingObjectRow object = service.createBillingObject(
                new BmsApplicationService.CreateBillingObjectCommand("BO-CARRIER", "承运商A", "CARRIER",
                        "PAYABLE", "CNY", 1001L, "bo-1"));
        BmsMapper.BillingRuleRow rule = service.createBillingRule(
                new BmsApplicationService.CreateBillingRuleCommand(object.objectCode(), "FREIGHT",
                        new BigDecimal("10.0000"), new BigDecimal("0.1000"), LocalDate.parse("2026-01-01"),
                        LocalDate.parse("2026-12-31"), 1001L, "br-1"));
        rule = service.publishBillingRule(rule.ruleNo(),
                new BmsApplicationService.VersionCommand(rule.version(), 1001L, "br-publish-1"));

        BmsApplicationService.CollectChargeSourceCommand sourceCommand =
                new BmsApplicationService.CollectChargeSourceCommand("TMS", "TMS-EVT-1", "src-1",
                        object.objectCode(), "FREIGHT", new BigDecimal("2.0000"), "2026-07", "{}", 1001L);
        BmsMapper.ChargeSourceRow source = service.collectChargeSource(sourceCommand);
        BmsMapper.ChargeSourceRow duplicate = service.collectChargeSource(sourceCommand);
        BmsMapper.ChargeDetailRow charge = service.listCharges(object.objectCode(), "2026-07", null).getFirst();

        charge = service.recalculateCharge(charge.chargeNo(),
                new BmsApplicationService.RecalculateChargeCommand(new BigDecimal("3.0000"), "数量修正",
                        charge.version(), 1001L, "recalc-1"));
        BmsMapper.AdjustmentRow adjustment = service.createAdjustment(
                new BmsApplicationService.CreateAdjustmentCommand(charge.chargeNo(), new BigDecimal("-3.00"),
                        "折扣调整", true, 1001L, "adj-1"));
        service.executeAdjustment(adjustment.adjustmentNo(),
                new BmsApplicationService.VersionCommand(adjustment.version(), 1001L, "adj-execute-1"));
        BmsMapper.ReconciliationRow reconciliation = service.generateReconciliation(
                new BmsApplicationService.GenerateReconciliationCommand(object.objectCode(), "2026-07", 1001L,
                        "rc-1"));
        reconciliation = service.confirmReconciliation(reconciliation.reconciliationNo(),
                new BmsApplicationService.ConfirmAmountCommand(reconciliation.totalAmount(), reconciliation.version(),
                        1001L, "rc-confirm-1"));
        BmsMapper.BillRow bill = service.generateBill(
                new BmsApplicationService.GenerateBillCommand(reconciliation.reconciliationNo(), 1001L, "bill-1"));
        bill = service.confirmBill(bill.billNo(),
                new BmsApplicationService.VersionCommand(bill.version(), 1001L, "bill-confirm-1"));
        BmsMapper.InvoiceRow invoice = service.requestInvoice(
                new BmsApplicationService.RequestInvoiceCommand(bill.billNo(), new BigDecimal("20.00"), 1001L,
                        "invoice-1"));
        invoice = service.issueInvoice(invoice.invoiceNo(),
                new BmsApplicationService.VersionCommand(invoice.version(), 1001L, "invoice-issue-1"));
        BmsMapper.FinanceHandoverRow finance = service.requestFinanceHandover(
                new BmsApplicationService.RequestFinanceCommand(bill.billNo(), 1001L, "finance-1"));
        finance = service.postFinanceHandover(finance.handoverNo(),
                new BmsApplicationService.PostFinanceCommand("ERP-V-1", finance.version(), 1001L, "finance-post-1"));
        BmsMapper.RefundSettlementRow refund = service.requestRefundSettlement(
                new BmsApplicationService.RequestRefundCommand(bill.billNo(), new BigDecimal("5.00"), 1001L,
                        "refund-1"));
        refund = service.finishRefundSettlement(refund.refundNo(),
                new BmsApplicationService.VersionCommand(refund.version(), 1001L, "refund-finish-1"));
        BmsMapper.InboxEventRow inbox = service.consumeEvent(
                new BmsApplicationService.ConsumeEventCommand("ERP", "ERP-EVT-1", "FinanceVoucherPosted",
                        finance.handoverNo(), "{}"));

        assertThat(source.status()).isEqualTo(BmsDomain.ChargeSourceAggregate.ACCEPTED);
        assertThat(duplicate.sourceNo()).isEqualTo(source.sourceNo());
        assertThat(rule.status()).isEqualTo(BmsDomain.BillingRuleAggregate.PUBLISHED);
        assertThat(reconciliation.status()).isEqualTo(BmsDomain.ReconciliationAggregate.CONFIRMED);
        assertThat(invoice.status()).isEqualTo(BmsDomain.InvoiceAggregate.ISSUED);
        assertThat(finance.status()).isEqualTo(BmsDomain.FinanceHandoverAggregate.POSTED);
        assertThat(refund.status()).isEqualTo(BmsDomain.RefundSettlementAggregate.FINISHED);
        assertThat(inbox.status()).isEqualTo(2);
        assertThat(service.settlementSummary(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))
                .billAmount()).isEqualByComparingTo("30.00");
        assertThat(mapper.events).extracting(BmsMapper.OutboxEventRow::eventType)
                .contains("BillingRulePublished", "ChargeCalculated", "ReconciliationConfirmed", "FinancialPosted");
    }

    @Test
    void failedChargeSourceCanReplayAfterRuleIsPublished() {
        MemoryBmsMapper mapper = new MemoryBmsMapper();
        BmsApplicationService service = new BmsApplicationService(mapper);
        BmsMapper.BillingObjectRow object = service.createBillingObject(
                new BmsApplicationService.CreateBillingObjectCommand("BO-WAREHOUSE", "仓库A", "WAREHOUSE",
                        "RECEIVABLE", "CNY", 1001L, "bo-1"));

        BmsMapper.ChargeSourceRow failed = service.collectChargeSource(
                new BmsApplicationService.CollectChargeSourceCommand("WMS", "WMS-EVT-1", "src-1",
                        object.objectCode(), "STORAGE", BigDecimal.ONE, "2026-07", "{}", 1001L));
        BmsMapper.BillingRuleRow rule = service.createBillingRule(
                new BmsApplicationService.CreateBillingRuleCommand(object.objectCode(), "STORAGE",
                        new BigDecimal("5.0000"), BigDecimal.ZERO, LocalDate.parse("2026-01-01"),
                        LocalDate.parse("2026-12-31"), 1001L, "rule-1"));
        service.publishBillingRule(rule.ruleNo(),
                new BmsApplicationService.VersionCommand(rule.version(), 1001L, "publish-1"));

        BmsMapper.ChargeSourceRow replayed = service.replayChargeSource(failed.sourceNo(),
                new BmsApplicationService.ReplayCommand(1001L, "replay-1"));

        assertThat(failed.status()).isEqualTo(BmsDomain.ChargeSourceAggregate.FAILED);
        assertThat(replayed.status()).isEqualTo(BmsDomain.ChargeSourceAggregate.ACCEPTED);
        assertThat(service.listCharges(object.objectCode(), "2026-07", null)).hasSize(1);
    }

    static class MemoryBmsMapper implements BmsMapper {
        final Map<String, BillingObjectRow> objects = new LinkedHashMap<>();
        final Map<String, BillingRuleRow> rules = new LinkedHashMap<>();
        final Map<String, ChargeSourceRow> sources = new LinkedHashMap<>();
        final Map<String, ChargeDetailRow> charges = new LinkedHashMap<>();
        final Map<String, AdjustmentRow> adjustments = new LinkedHashMap<>();
        final Map<String, ReconciliationRow> reconciliations = new LinkedHashMap<>();
        final Map<String, BillRow> bills = new LinkedHashMap<>();
        final Map<String, InvoiceRow> invoices = new LinkedHashMap<>();
        final Map<String, FinanceHandoverRow> finances = new LinkedHashMap<>();
        final Map<String, RefundSettlementRow> refunds = new LinkedHashMap<>();
        final Map<String, InboxEventRow> inboxes = new LinkedHashMap<>();
        final List<OutboxEventRow> events = new ArrayList<>();
        final List<OperationLogRow> logs = new ArrayList<>();

        @Override
        public BillingObjectRow findBillingObject(String objectCode) {
            return objects.get(objectCode);
        }

        @Override
        public List<BillingObjectRow> listBillingObjects(Integer status) {
            return objects.values().stream().filter(row -> status == null || row.status() == status).toList();
        }

        @Override
        public void insertBillingObject(BillingObjectRow row) {
            objects.put(row.objectCode(), row);
        }

        @Override
        public void updateBillingObject(BillingObjectRow row) {
            objects.put(row.objectCode(), row);
        }

        @Override
        public BillingRuleRow findBillingRule(String ruleNo) {
            return rules.get(ruleNo);
        }

        @Override
        public BillingRuleRow findPublishedRule(String objectCode, String feeType) {
            return rules.values().stream()
                    .filter(row -> row.objectCode().equals(objectCode)
                            && row.feeType().equals(feeType)
                            && row.status() == BmsDomain.BillingRuleAggregate.PUBLISHED)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int countPublishedRuleOverlap(String objectCode, String feeType, LocalDate effectiveFrom,
                                             LocalDate effectiveTo) {
            return (int) rules.values().stream()
                    .filter(row -> row.objectCode().equals(objectCode)
                            && row.feeType().equals(feeType)
                            && row.status() == BmsDomain.BillingRuleAggregate.PUBLISHED
                            && !row.effectiveFrom().isAfter(effectiveTo)
                            && !row.effectiveTo().isBefore(effectiveFrom))
                    .count();
        }

        @Override
        public List<BillingRuleRow> listBillingRules(String objectCode) {
            return rules.values().stream().filter(row -> row.objectCode().equals(objectCode)).toList();
        }

        @Override
        public void insertBillingRule(BillingRuleRow row) {
            rules.put(row.ruleNo(), row);
        }

        @Override
        public void updateBillingRule(BillingRuleRow row) {
            rules.put(row.ruleNo(), row);
        }

        @Override
        public ChargeSourceRow findChargeSource(String sourceNo) {
            return sources.get(sourceNo);
        }

        @Override
        public ChargeSourceRow findChargeSourceByIdempotency(String sourceSystem, String idempotencyKey) {
            return sources.values().stream()
                    .filter(row -> row.sourceSystem().equals(sourceSystem)
                            && row.idempotencyKey().equals(idempotencyKey))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<ChargeSourceRow> listChargeSources(Integer status) {
            return sources.values().stream().filter(row -> status == null || row.status() == status).toList();
        }

        @Override
        public void insertChargeSource(ChargeSourceRow row) {
            sources.put(row.sourceNo(), row);
        }

        @Override
        public void updateChargeSource(ChargeSourceRow row) {
            sources.put(row.sourceNo(), row);
        }

        @Override
        public ChargeDetailRow findChargeDetail(String chargeNo) {
            return charges.get(chargeNo);
        }

        @Override
        public ChargeDetailRow findChargeBySource(String sourceNo) {
            return charges.values().stream()
                    .filter(row -> row.sourceNo().equals(sourceNo))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<ChargeDetailRow> listCharges(String objectCode, String billingPeriod, Integer status) {
            return charges.values().stream()
                    .filter(row -> row.objectCode().equals(objectCode)
                            && row.billingPeriod().equals(billingPeriod)
                            && (status == null || row.status() == status))
                    .toList();
        }

        @Override
        public void insertChargeDetail(ChargeDetailRow row) {
            charges.put(row.chargeNo(), row);
        }

        @Override
        public void updateChargeDetail(ChargeDetailRow row) {
            charges.put(row.chargeNo(), row);
        }

        @Override
        public void markChargesConfirmed(String objectCode, String billingPeriod) {
            for (ChargeDetailRow row : new ArrayList<>(charges.values())) {
                if (row.objectCode().equals(objectCode)
                        && row.billingPeriod().equals(billingPeriod)
                        && row.status() == BmsDomain.ChargeDetailAggregate.PENDING_RECONCILIATION) {
                    charges.put(row.chargeNo(), new ChargeDetailRow(row.id(), row.chargeNo(), row.sourceNo(),
                            row.objectCode(), row.feeType(), row.ruleNo(), row.quantity(), row.unitPrice(),
                            row.amount(), row.taxAmount(), row.totalAmount(), row.billingPeriod(),
                            BmsDomain.ChargeDetailAggregate.CONFIRMED, row.version() + 1));
                }
            }
        }

        @Override
        public void insertAdjustment(AdjustmentRow row) {
            adjustments.put(row.adjustmentNo(), row);
        }

        @Override
        public AdjustmentRow findAdjustment(String adjustmentNo) {
            return adjustments.get(adjustmentNo);
        }

        @Override
        public void updateAdjustment(AdjustmentRow row) {
            adjustments.put(row.adjustmentNo(), row);
        }

        @Override
        public void insertReconciliation(ReconciliationRow row) {
            reconciliations.put(row.reconciliationNo(), row);
        }

        @Override
        public ReconciliationRow findReconciliation(String reconciliationNo) {
            return reconciliations.get(reconciliationNo);
        }

        @Override
        public void updateReconciliation(ReconciliationRow row) {
            reconciliations.put(row.reconciliationNo(), row);
        }

        @Override
        public void insertBill(BillRow row) {
            bills.put(row.billNo(), row);
        }

        @Override
        public BillRow findBill(String billNo) {
            return bills.get(billNo);
        }

        @Override
        public void updateBill(BillRow row) {
            bills.put(row.billNo(), row);
        }

        @Override
        public void insertInvoice(InvoiceRow row) {
            invoices.put(row.invoiceNo(), row);
        }

        @Override
        public InvoiceRow findInvoice(String invoiceNo) {
            return invoices.get(invoiceNo);
        }

        @Override
        public void updateInvoice(InvoiceRow row) {
            invoices.put(row.invoiceNo(), row);
        }

        @Override
        public void insertFinanceHandover(FinanceHandoverRow row) {
            finances.put(row.handoverNo(), row);
        }

        @Override
        public FinanceHandoverRow findFinanceHandover(String handoverNo) {
            return finances.get(handoverNo);
        }

        @Override
        public void updateFinanceHandover(FinanceHandoverRow row) {
            finances.put(row.handoverNo(), row);
        }

        @Override
        public void insertRefundSettlement(RefundSettlementRow row) {
            refunds.put(row.refundNo(), row);
        }

        @Override
        public RefundSettlementRow findRefundSettlement(String refundNo) {
            return refunds.get(refundNo);
        }

        @Override
        public void updateRefundSettlement(RefundSettlementRow row) {
            refunds.put(row.refundNo(), row);
        }

        @Override
        public void insertOutboxEvent(OutboxEventRow row) {
            events.add(row);
        }

        @Override
        public List<OutboxEventRow> listOutboxEvents() {
            return events;
        }

        @Override
        public InboxEventRow findInboxEvent(String sourceSystem, String sourceEventId) {
            return inboxes.get(sourceSystem + ":" + sourceEventId);
        }

        @Override
        public void insertInboxEvent(InboxEventRow row) {
            inboxes.put(row.sourceSystem() + ":" + row.sourceEventId(), row);
        }

        @Override
        public void updateInboxEvent(InboxEventRow row) {
            inboxes.put(row.sourceSystem() + ":" + row.sourceEventId(), row);
        }

        @Override
        public void insertOperationLog(OperationLogRow row) {
            logs.add(row);
        }

        @Override
        public SettlementSummaryRow settlementSummary(LocalDateTime from, LocalDateTime to) {
            BigDecimal total = bills.values().stream()
                    .map(BillRow::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new SettlementSummaryRow(total, bills.size());
        }
    }
}
