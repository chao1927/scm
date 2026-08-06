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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BmsApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class BmsApplicationServiceTest {

    /**
     * 处理当前类型职责中的操作 {@code protectsCumulativeRefundAndConsumesPaymentReceiptIdempotently}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void protectsCumulativeRefundAndConsumesPaymentReceiptIdempotently() {
        MemoryBmsMapper mapper = new MemoryBmsMapper();
        mapper.objects.put("OBJ", new BmsMapper.BillingObjectRow(1L, "OBJ", "测试对象",
            "CUSTOMER", "PAYABLE", "CNY", 1, 1));
        mapper.bills.put("B-1", new BmsMapper.BillRow(1L, "B-1", "R-1", "OBJ", new BigDecimal("100"), 2, 1));
        BmsApplicationService service = new BmsApplicationService(mapper);
        var first = service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand("B-1", new BigDecimal("60"), 1L, "refund-1"));
        assertThatThrownBy(() -> service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand("B-1", new BigDecimal("50"), 1L, "refund-2"))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("refundable");
        var failed = service.consumeRefundReceipt(first.refundNo(), receipt("PAY-FAIL-1", false, "渠道失败", "60.00"));
        var retried = service.retryRefundSettlement(first.refundNo(), new BmsApplicationService.VersionCommand(failed.version(), 1L, "retry-1"));
        var finished = service.consumeRefundReceipt(first.refundNo(), receipt("PAY-OK-1", true, null, "60.00"));
        var duplicate = service.consumeRefundReceipt(first.refundNo(), receipt("PAY-OK-1", true, null, "60.00"));
        assertThat(retried.status()).isEqualTo(BmsDomain.RefundSettlementAggregate.REQUESTED);
        assertThat(finished.status()).isEqualTo(BmsDomain.RefundSettlementAggregate.FINISHED);
        assertThat(duplicate.version()).isEqualTo(finished.version());
    }

    @Test
    void keepsPendingRefundOccupiedAndRequiresTwoPersonEvidenceForManualClose() {
        MemoryBmsMapper mapper = refundFixture();
        BmsApplicationService service = new BmsApplicationService(mapper);
        var refund = service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand(
            "B-1", new BigDecimal("60.00"), 1L, "refund-pending"));
        var pending = service.markRefundConfirmationPending(refund.refundNo(),
            new BmsApplicationService.ConfirmationPendingCommand(
                "payment timeout", refund.version(), 1L, "pending-1"));

        assertThatThrownBy(() -> service.requestRefundSettlement(
            new BmsApplicationService.RequestRefundCommand("B-1", new BigDecimal("50.00"),
                1L, "refund-over")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("refundable");
        assertThatThrownBy(() -> service.closeRefundManually(pending.refundNo(),
            new BmsApplicationService.ManualRefundResolutionCommand("verified unpaid", "ticket-1",
                pending.version(), 1L, 1L, "close-1")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("two different");

        var closed = service.closeRefundManually(pending.refundNo(),
            new BmsApplicationService.ManualRefundResolutionCommand("verified unpaid", "ticket-1",
                pending.version(), 1L, 2L, "close-1"));
        assertThat(closed.status()).isEqualTo(BmsDomain.RefundSettlementAggregate.CLOSED);
        assertThat(service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand(
            "B-1", new BigDecimal("50.00"), 1L, "refund-after-close"))).isNotNull();
    }

    @Test
    void rejectsReceiptFactsAndRequestIdempotencyConflicts() {
        MemoryBmsMapper mapper = refundFixture();
        BmsApplicationService service = new BmsApplicationService(mapper);
        var first = service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand(
            "B-1", new BigDecimal("40.00"), 1L, "same-key"));
        assertThat(service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand(
            "B-1", new BigDecimal("40.00"), 1L, "same-key")).refundNo()).isEqualTo(first.refundNo());
        assertThatThrownBy(() -> service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand(
            "B-1", new BigDecimal("30.00"), 1L, "same-key")))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("conflicts");

        var unchanged = service.consumeRefundReceipt(first.refundNo(),
            receipt("BAD-AMOUNT", true, null, "39.00"));
        assertThat(unchanged.status()).isEqualTo(BmsDomain.RefundSettlementAggregate.REQUESTED);
        assertThat(mapper.refundExceptions).hasSize(1);
    }

    private static BmsApplicationService.RefundReceiptCommand receipt(
            String receiptNo, boolean success, String reason, String amount) {
        return new BmsApplicationService.RefundReceiptCommand(receiptNo, success, reason,
            new BigDecimal(amount), "CNY", "OBJ", "TX-" + receiptNo, "{}");
    }

    private static MemoryBmsMapper refundFixture() {
        MemoryBmsMapper mapper = new MemoryBmsMapper();
        mapper.objects.put("OBJ", new BmsMapper.BillingObjectRow(1L, "OBJ", "测试对象",
            "CUSTOMER", "PAYABLE", "CNY", 1, 1));
        mapper.bills.put("B-1", new BmsMapper.BillRow(1L, "B-1", "R-1", "OBJ",
            new BigDecimal("100.00"), 2, 1));
        return mapper;
    }

    /**
     * 执行命令 {@code completesBillingSettlementFinanceAndRefundLoop}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void completesBillingSettlementFinanceAndRefundLoop() {
        MemoryBmsMapper mapper = new MemoryBmsMapper();
        BmsApplicationService service = new BmsApplicationService(mapper);
        BmsMapper.BillingObjectRow object = service.createBillingObject(new BmsApplicationService.CreateBillingObjectCommand("BO-CARRIER", "承运商A", "CARRIER", "PAYABLE", "CNY", 1001L, "bo-1"));
        BmsMapper.BillingRuleRow rule = service.createBillingRule(new BmsApplicationService.CreateBillingRuleCommand(object.objectCode(), "FREIGHT", new BigDecimal("10.0000"), new BigDecimal("0.1000"), LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"), 1001L, "br-1"));
        rule = service.publishBillingRule(rule.ruleNo(), new BmsApplicationService.VersionCommand(rule.version(), 1001L, "br-publish-1"));
        BmsApplicationService.CollectChargeSourceCommand sourceCommand = new BmsApplicationService.CollectChargeSourceCommand("TMS", "TMS-EVT-1", "src-1", object.objectCode(), "FREIGHT", new BigDecimal("2.0000"), "2026-07", "{}", 1001L);
        BmsMapper.ChargeSourceRow source = service.collectChargeSource(sourceCommand);
        BmsMapper.ChargeSourceRow duplicate = service.collectChargeSource(sourceCommand);
        BmsMapper.ChargeDetailRow charge = service.listCharges(object.objectCode(), "2026-07", null).get(0);
        charge = service.recalculateCharge(charge.chargeNo(), new BmsApplicationService.RecalculateChargeCommand(new BigDecimal("3.0000"), "数量修正", charge.version(), 1001L, "recalc-1"));
        BmsMapper.AdjustmentRow adjustment = service.createAdjustment(new BmsApplicationService.CreateAdjustmentCommand(charge.chargeNo(), new BigDecimal("-3.00"), "折扣调整", true, 1001L, "adj-1"));
        service.executeAdjustment(adjustment.adjustmentNo(), new BmsApplicationService.VersionCommand(adjustment.version(), 1001L, "adj-execute-1"));
        BmsMapper.ReconciliationRow reconciliation = service.generateReconciliation(new BmsApplicationService.GenerateReconciliationCommand(object.objectCode(), "2026-07", 1001L, "rc-1"));
        reconciliation = service.confirmReconciliation(reconciliation.reconciliationNo(), new BmsApplicationService.ConfirmAmountCommand(reconciliation.totalAmount(), reconciliation.version(), 1001L, "rc-confirm-1"));
        BmsMapper.BillRow bill = service.generateBill(new BmsApplicationService.GenerateBillCommand(reconciliation.reconciliationNo(), 1001L, "bill-1"));
        bill = service.confirmBill(bill.billNo(), new BmsApplicationService.VersionCommand(bill.version(), 1001L, "bill-confirm-1"));
        BmsMapper.InvoiceRow invoice = service.requestInvoice(new BmsApplicationService.RequestInvoiceCommand(bill.billNo(), new BigDecimal("20.00"), 1001L, "invoice-1"));
        invoice = service.issueInvoice(invoice.invoiceNo(), new BmsApplicationService.VersionCommand(invoice.version(), 1001L, "invoice-issue-1"));
        BmsMapper.FinanceHandoverRow finance = service.requestFinanceHandover(new BmsApplicationService.RequestFinanceCommand(bill.billNo(), 1001L, "finance-1"));
        finance = service.postFinanceHandover(finance.handoverNo(), new BmsApplicationService.PostFinanceCommand("ERP-V-1", finance.version(), 1001L, "finance-post-1"));
        BmsMapper.RefundSettlementRow refund = service.requestRefundSettlement(new BmsApplicationService.RequestRefundCommand(bill.billNo(), new BigDecimal("5.00"), 1001L, "refund-1"));
        refund = service.finishRefundSettlement(refund.refundNo(), new BmsApplicationService.VersionCommand(refund.version(), 1001L, "refund-finish-1"));
        BmsMapper.InboxEventRow inbox = service.consumeEvent(new BmsApplicationService.ConsumeEventCommand("ERP", "ERP-EVT-1", "FinanceVoucherPosted", finance.handoverNo(), "{}"));
        assertThat(source.status()).isEqualTo(BmsDomain.ChargeSourceAggregate.ACCEPTED);
        assertThat(duplicate.sourceNo()).isEqualTo(source.sourceNo());
        assertThat(rule.status()).isEqualTo(BmsDomain.BillingRuleAggregate.PUBLISHED);
        assertThat(reconciliation.status()).isEqualTo(BmsDomain.ReconciliationAggregate.CONFIRMED);
        assertThat(invoice.status()).isEqualTo(BmsDomain.InvoiceAggregate.ISSUED);
        assertThat(finance.status()).isEqualTo(BmsDomain.FinanceHandoverAggregate.POSTED);
        assertThat(refund.status()).isEqualTo(BmsDomain.RefundSettlementAggregate.FINISHED);
        assertThat(inbox.status()).isEqualTo(2);
        assertThat(service.settlementSummary(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)).billAmount()).isEqualByComparingTo("30.00");
        assertThat(mapper.events).extracting(BmsMapper.OutboxEventRow::eventType).contains("BillingRulePublished", "ChargeCalculated", "ReconciliationConfirmed", "FinancialPosted");
    }

    /**
     * 处理当前类型职责中的操作 {@code failedChargeSourceCanReplayAfterRuleIsPublished}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void failedChargeSourceCanReplayAfterRuleIsPublished() {
        MemoryBmsMapper mapper = new MemoryBmsMapper();
        BmsApplicationService service = new BmsApplicationService(mapper);
        BmsMapper.BillingObjectRow object = service.createBillingObject(new BmsApplicationService.CreateBillingObjectCommand("BO-WAREHOUSE", "仓库A", "WAREHOUSE", "RECEIVABLE", "CNY", 1001L, "bo-1"));
        BmsMapper.ChargeSourceRow failed = service.collectChargeSource(new BmsApplicationService.CollectChargeSourceCommand("WMS", "WMS-EVT-1", "src-1", object.objectCode(), "STORAGE", BigDecimal.ONE, "2026-07", "{}", 1001L));
        BmsMapper.BillingRuleRow rule = service.createBillingRule(new BmsApplicationService.CreateBillingRuleCommand(object.objectCode(), "STORAGE", new BigDecimal("5.0000"), BigDecimal.ZERO, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"), 1001L, "rule-1"));
        service.publishBillingRule(rule.ruleNo(), new BmsApplicationService.VersionCommand(rule.version(), 1001L, "publish-1"));
        BmsMapper.ChargeSourceRow replayed = service.replayChargeSource(failed.sourceNo(), new BmsApplicationService.ReplayCommand(1001L, "replay-1"));
        assertThat(failed.status()).isEqualTo(BmsDomain.ChargeSourceAggregate.FAILED);
        assertThat(replayed.status()).isEqualTo(BmsDomain.ChargeSourceAggregate.ACCEPTED);
        assertThat(service.listCharges(object.objectCode(), "2026-07", null)).hasSize(1);
    }

    /**
     * MemoryBmsMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class MemoryBmsMapper implements BmsMapper {

        /**
         * objects（类型：{@code Map<String,BillingObjectRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, BillingObjectRow> objects = new LinkedHashMap<>();

        /**
         * rules（类型：{@code Map<String,BillingRuleRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, BillingRuleRow> rules = new LinkedHashMap<>();

        /**
         * sources（类型：{@code Map<String,ChargeSourceRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, ChargeSourceRow> sources = new LinkedHashMap<>();

        /**
         * charges（类型：{@code Map<String,ChargeDetailRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, ChargeDetailRow> charges = new LinkedHashMap<>();

        /**
         * adjustments（类型：{@code Map<String,AdjustmentRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, AdjustmentRow> adjustments = new LinkedHashMap<>();

        /**
         * reconciliations（类型：{@code Map<String,ReconciliationRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, ReconciliationRow> reconciliations = new LinkedHashMap<>();

        /**
         * bills（类型：{@code Map<String,BillRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, BillRow> bills = new LinkedHashMap<>();

        /**
         * invoices（类型：{@code Map<String,InvoiceRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, InvoiceRow> invoices = new LinkedHashMap<>();

        /**
         * finances（类型：{@code Map<String,FinanceHandoverRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, FinanceHandoverRow> finances = new LinkedHashMap<>();

        /**
         * refunds（类型：{@code Map<String,RefundSettlementRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, RefundSettlementRow> refunds = new LinkedHashMap<>();

        /**
         * refundReceipts（类型：{@code Map<String,String>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, String> refundReceipts = new LinkedHashMap<>();

        final Map<String, RefundReceiptRow> refundReceiptRows = new LinkedHashMap<>();

        final List<RefundExceptionRow> refundExceptions = new ArrayList<>();

        /**
         * inboxes（类型：{@code Map<String,InboxEventRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final Map<String, InboxEventRow> inboxes = new LinkedHashMap<>();

        /**
         * events（类型：{@code List<OutboxEventRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OutboxEventRow> events = new ArrayList<>();

        /**
         * logs（类型：{@code List<OperationLogRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        final List<OperationLogRow> logs = new ArrayList<>();

        /**
         * 查询并返回 {@code findBillingObject}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code BillingObjectRow}
         */
        @Override
        public BillingObjectRow findBillingObject(String objectCode) {
            return objects.get(objectCode);
        }

        /**
         * 查询并返回 {@code listBillingObjects}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param status 生命周期状态，类型为 {@code Integer}
         * @return 查询并返回的结果，类型为 {@code List<BillingObjectRow>}
         */
        @Override
        public List<BillingObjectRow> listBillingObjects(Integer status) {
            return objects.values().stream().filter(row -> status == null || row.status() == status).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertBillingObject}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code BillingObjectRow}
         */
        @Override
        public void insertBillingObject(BillingObjectRow row) {
            objects.put(row.objectCode(), row);
        }

        /**
         * 执行命令 {@code updateBillingObject}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code BillingObjectRow}
         */
        @Override
        public void updateBillingObject(BillingObjectRow row) {
            objects.put(row.objectCode(), row);
        }

        /**
         * 查询并返回 {@code findBillingRule}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param ruleNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code BillingRuleRow}
         */
        @Override
        public BillingRuleRow findBillingRule(String ruleNo) {
            return rules.get(ruleNo);
        }

        /**
         * 查询并返回 {@code findPublishedRule}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code BillingRuleRow}
         */
        @Override
        public BillingRuleRow findPublishedRule(String objectCode, String feeType) {
            return rules.values().stream().filter(row -> row.objectCode().equals(objectCode) && row.feeType().equals(feeType) && row.status() == BmsDomain.BillingRuleAggregate.PUBLISHED).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code countPublishedRuleOverlap}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param feeType 金额或计费值，类型为 {@code String}
         * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
         * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
         * @return 查询并返回的结果，类型为 {@code int}
         */
        @Override
        public int countPublishedRuleOverlap(String objectCode, String feeType, LocalDate effectiveFrom, LocalDate effectiveTo) {
            return (int) rules.values().stream().filter(row -> row.objectCode().equals(objectCode) && row.feeType().equals(feeType) && row.status() == BmsDomain.BillingRuleAggregate.PUBLISHED && !row.effectiveFrom().isAfter(effectiveTo) && !row.effectiveTo().isBefore(effectiveFrom)).count();
        }

        /**
         * 查询并返回 {@code listBillingRules}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code List<BillingRuleRow>}
         */
        @Override
        public List<BillingRuleRow> listBillingRules(String objectCode) {
            return rules.values().stream().filter(row -> row.objectCode().equals(objectCode)).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertBillingRule}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code BillingRuleRow}
         */
        @Override
        public void insertBillingRule(BillingRuleRow row) {
            rules.put(row.ruleNo(), row);
        }

        /**
         * 执行命令 {@code updateBillingRule}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code BillingRuleRow}
         */
        @Override
        public void updateBillingRule(BillingRuleRow row) {
            rules.put(row.ruleNo(), row);
        }

        /**
         * 查询并返回 {@code findChargeSource}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ChargeSourceRow}
         */
        @Override
        public ChargeSourceRow findChargeSource(String sourceNo) {
            return sources.get(sourceNo);
        }

        /**
         * 查询并返回 {@code findChargeSourceByIdempotency}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param idempotencyKey 业务或技术标识，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ChargeSourceRow}
         */
        @Override
        public ChargeSourceRow findChargeSourceByIdempotency(String sourceSystem, String idempotencyKey) {
            return sources.values().stream().filter(row -> row.sourceSystem().equals(sourceSystem) && row.idempotencyKey().equals(idempotencyKey)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listChargeSources}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param status 生命周期状态，类型为 {@code Integer}
         * @return 查询并返回的结果，类型为 {@code List<ChargeSourceRow>}
         */
        @Override
        public List<ChargeSourceRow> listChargeSources(Integer status) {
            return sources.values().stream().filter(row -> status == null || row.status() == status).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertChargeSource}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ChargeSourceRow}
         */
        @Override
        public void insertChargeSource(ChargeSourceRow row) {
            sources.put(row.sourceNo(), row);
        }

        /**
         * 执行命令 {@code updateChargeSource}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ChargeSourceRow}
         */
        @Override
        public void updateChargeSource(ChargeSourceRow row) {
            sources.put(row.sourceNo(), row);
        }

        /**
         * 查询并返回 {@code findChargeDetail}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param chargeNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ChargeDetailRow}
         */
        @Override
        public ChargeDetailRow findChargeDetail(String chargeNo) {
            return charges.get(chargeNo);
        }

        /**
         * 查询并返回 {@code findChargeBySource}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ChargeDetailRow}
         */
        @Override
        public ChargeDetailRow findChargeBySource(String sourceNo) {
            return charges.values().stream().filter(row -> row.sourceNo().equals(sourceNo)).findFirst().orElse(null);
        }

        /**
         * 查询并返回 {@code listCharges}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code Integer}
         * @return 查询并返回的结果，类型为 {@code List<ChargeDetailRow>}
         */
        @Override
        public List<ChargeDetailRow> listCharges(String objectCode, String billingPeriod, Integer status) {
            return charges.values().stream().filter(row -> row.objectCode().equals(objectCode) && row.billingPeriod().equals(billingPeriod) && (status == null || row.status() == status)).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code insertChargeDetail}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ChargeDetailRow}
         */
        @Override
        public void insertChargeDetail(ChargeDetailRow row) {
            charges.put(row.chargeNo(), row);
        }

        /**
         * 执行命令 {@code updateChargeDetail}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ChargeDetailRow}
         */
        @Override
        public void updateChargeDetail(ChargeDetailRow row) {
            charges.put(row.chargeNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code markChargesConfirmed}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param objectCode 可追踪业务编码，类型为 {@code String}
         * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void markChargesConfirmed(String objectCode, String billingPeriod) {
            for (ChargeDetailRow row : new ArrayList<>(charges.values())) {
                if (row.objectCode().equals(objectCode) && row.billingPeriod().equals(billingPeriod) && row.status() == BmsDomain.ChargeDetailAggregate.PENDING_RECONCILIATION) {
                    charges.put(row.chargeNo(), new ChargeDetailRow(row.id(), row.chargeNo(), row.sourceNo(), row.objectCode(), row.feeType(), row.ruleNo(), row.quantity(), row.unitPrice(), row.amount(), row.taxAmount(), row.totalAmount(), row.billingPeriod(), BmsDomain.ChargeDetailAggregate.CONFIRMED, row.version() + 1));
                }
            }
        }

        /**
         * 处理当前类型职责中的操作 {@code insertAdjustment}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code AdjustmentRow}
         */
        @Override
        public void insertAdjustment(AdjustmentRow row) {
            adjustments.put(row.adjustmentNo(), row);
        }

        /**
         * 查询并返回 {@code findAdjustment}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param adjustmentNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code AdjustmentRow}
         */
        @Override
        public AdjustmentRow findAdjustment(String adjustmentNo) {
            return adjustments.get(adjustmentNo);
        }

        /**
         * 执行命令 {@code updateAdjustment}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code AdjustmentRow}
         */
        @Override
        public void updateAdjustment(AdjustmentRow row) {
            adjustments.put(row.adjustmentNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertReconciliation}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ReconciliationRow}
         */
        @Override
        public void insertReconciliation(ReconciliationRow row) {
            reconciliations.put(row.reconciliationNo(), row);
        }

        /**
         * 查询并返回 {@code findReconciliation}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param reconciliationNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReconciliationRow}
         */
        @Override
        public ReconciliationRow findReconciliation(String reconciliationNo) {
            return reconciliations.get(reconciliationNo);
        }

        /**
         * 执行命令 {@code updateReconciliation}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code ReconciliationRow}
         */
        @Override
        public void updateReconciliation(ReconciliationRow row) {
            reconciliations.put(row.reconciliationNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertBill}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code BillRow}
         */
        @Override
        public void insertBill(BillRow row) {
            bills.put(row.billNo(), row);
        }

        /**
         * 查询并返回 {@code findBill}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code BillRow}
         */
        @Override
        public BillRow findBill(String billNo) {
            return bills.get(billNo);
        }

        /**
         * 执行命令 {@code updateBill}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code BillRow}
         */
        @Override
        public void updateBill(BillRow row) {
            bills.put(row.billNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertInvoice}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code InvoiceRow}
         */
        @Override
        public void insertInvoice(InvoiceRow row) {
            invoices.put(row.invoiceNo(), row);
        }

        /**
         * 查询并返回 {@code findInvoice}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param invoiceNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code InvoiceRow}
         */
        @Override
        public InvoiceRow findInvoice(String invoiceNo) {
            return invoices.get(invoiceNo);
        }

        /**
         * 执行命令 {@code updateInvoice}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code InvoiceRow}
         */
        @Override
        public void updateInvoice(InvoiceRow row) {
            invoices.put(row.invoiceNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertFinanceHandover}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code FinanceHandoverRow}
         */
        @Override
        public void insertFinanceHandover(FinanceHandoverRow row) {
            finances.put(row.handoverNo(), row);
        }

        /**
         * 查询并返回 {@code findFinanceHandover}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param handoverNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code FinanceHandoverRow}
         */
        @Override
        public FinanceHandoverRow findFinanceHandover(String handoverNo) {
            return finances.get(handoverNo);
        }

        /**
         * 执行命令 {@code updateFinanceHandover}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code FinanceHandoverRow}
         */
        @Override
        public void updateFinanceHandover(FinanceHandoverRow row) {
            finances.put(row.handoverNo(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertRefundSettlement}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code RefundSettlementRow}
         */
        @Override
        public void insertRefundSettlement(RefundSettlementRow row) {
            refunds.put(row.refundNo(), row);
        }

        /**
         * 查询并返回 {@code findRefundSettlement}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param refundNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code RefundSettlementRow}
         */
        @Override
        public RefundSettlementRow findRefundSettlement(String refundNo) {
            return refunds.get(refundNo);
        }

        @Override
        public RefundSettlementRow findRefundByIdempotencyKey(String idempotencyKey) {
            return refunds.values().stream()
                .filter(row -> idempotencyKey.equals(row.requestIdempotencyKey()))
                .findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code lockBill}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BillRow}
         */
        @Override
        public BillRow lockBill(String billNo) {
            return bills.get(billNo);
        }

        /**
         * 处理当前类型职责中的操作 {@code occupiedRefundAmount}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param billNo 可追踪业务编码，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        @Override
        public BigDecimal occupiedRefundAmount(String billNo) {
            return refunds.values().stream().filter(row -> row.billNo().equals(billNo))
                .filter(row -> row.status() == 1 || row.status() == 2 || row.status() == 4)
                .map(RefundSettlementRow::refundAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        /**
         * 执行命令 {@code updateRefundSettlement}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code RefundSettlementRow}
         */
        @Override
        public int updateRefundSettlement(RefundSettlementRow row) {
            RefundSettlementRow current = refunds.get(row.refundNo());
            if (current == null || current.version() + 1 != row.version()) {
                return 0;
            }
            refunds.put(row.refundNo(), row);
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code claimRefundReceipt}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param receiptNo 可追踪业务编码，类型为 {@code String}
         * @param refundNo 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int claimRefundReceipt(String receiptNo, String refundNo, String status,
                                      BigDecimal refundAmount, String currency, String merchantNo,
                                      String paymentTxnNo, String failureReason, String payload) {
            if (refundReceipts.putIfAbsent(receiptNo, refundNo) != null) {
                return 0;
            }
            refundReceiptRows.put(receiptNo, new RefundReceiptRow(receiptNo, refundNo, status,
                refundAmount, currency, merchantNo, paymentTxnNo, failureReason, payload));
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code hasRefundReceipt}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param receiptNo 可追踪业务编码，类型为 {@code String}
         * @param refundNo 可追踪业务编码，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int hasRefundReceipt(String receiptNo, String refundNo) {
            return refundNo.equals(refundReceipts.get(receiptNo)) ? 1 : 0;
        }

        @Override
        public RefundReceiptRow findRefundReceipt(String receiptNo) {
            return refundReceiptRows.get(receiptNo);
        }

        @Override
        public void insertRefundException(RefundExceptionRow row) {
            refundExceptions.add(row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOutboxEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OutboxEventRow}
         */
        @Override
        public void insertOutboxEvent(OutboxEventRow row) {
            events.add(row);
        }

        /**
         * 查询并返回 {@code listOutboxEvents}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @return 查询并返回的结果，类型为 {@code List<OutboxEventRow>}
         */
        @Override
        public List<OutboxEventRow> listOutboxEvents() {
            return events;
        }

        /**
         * 查询并返回 {@code findInboxEvent}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceEventId 业务或技术标识，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code InboxEventRow}
         */
        @Override
        public InboxEventRow findInboxEvent(String sourceSystem, String sourceEventId) {
            return inboxes.get(sourceSystem + ":" + sourceEventId);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertInboxEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code InboxEventRow}
         */
        @Override
        public void insertInboxEvent(InboxEventRow row) {
            inboxes.put(row.sourceSystem() + ":" + row.sourceEventId(), row);
        }

        /**
         * 执行命令 {@code updateInboxEvent}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code InboxEventRow}
         */
        @Override
        public void updateInboxEvent(InboxEventRow row) {
            inboxes.put(row.sourceSystem() + ":" + row.sourceEventId(), row);
        }

        /**
         * 处理当前类型职责中的操作 {@code insertOperationLog}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param row 业务处理参数或成员，类型为 {@code OperationLogRow}
         */
        @Override
        public void insertOperationLog(OperationLogRow row) {
            logs.add(row);
        }

        /**
         * 处理当前类型职责中的操作 {@code settlementSummary}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param from 业务处理参数或成员，类型为 {@code LocalDateTime}
         * @param to 业务处理参数或成员，类型为 {@code LocalDateTime}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code SettlementSummaryRow}
         */
        @Override
        public SettlementSummaryRow settlementSummary(LocalDateTime from, LocalDateTime to) {
            BigDecimal total = bills.values().stream().map(BillRow::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new SettlementSummaryRow(total, bills.size());
        }
    }
}
