package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.application.integration.BmsExternalIntegrationApplicationService;
import com.chaobo.scm.bms.application.integration.ErpFinanceGateway;
import com.chaobo.scm.bms.application.integration.PaymentCallbackApplicationService;
import com.chaobo.scm.bms.application.integration.PaymentGateway;
import com.chaobo.scm.bms.application.integration.TaxInvoiceGateway;
import com.chaobo.scm.bms.domain.BmsDomain;
import com.chaobo.scm.bms.infrastructure.persistence.BmsExternalTaskMapper;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BmsExternalIntegrationApplicationServiceTest {

    @Test
    void erpDtoStopsAtGatewayAndSuccessfulTaskPostsFinance() {
        var mapper = new BmsApplicationServiceTest.MemoryBmsMapper();
        mapper.bills.put("B-1", new BmsMapper.BillRow(
            1L, "B-1", "RC-1", "OBJ", new BigDecimal("100.00"), 2, 1));
        mapper.finances.put("FH-1", new BmsMapper.FinanceHandoverRow(
            1L, "FH-1", "B-1", BmsDomain.FinanceHandoverAggregate.REQUESTED,
            null, null, 1));
        var taskMapper = new MemoryTaskMapper();
        var service = integration(mapper, taskMapper,
            request -> new ErpFinanceGateway.PostingResult("VOUCHER-1"),
            request -> new TaxInvoiceGateway.IssueResult("TAX-1"),
            request -> new PaymentGateway.RefundResult("PAY-1"));

        var task = service.enqueueErpPosting("FH-1", "erp-idem-1");
        var duplicate = service.enqueueErpPosting("FH-1", "erp-idem-1");
        var result = service.dispatch(10);

        assertThat(duplicate.taskNo()).isEqualTo(task.taskNo());
        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(mapper.finances.get("FH-1").status())
            .isEqualTo(BmsDomain.FinanceHandoverAggregate.POSTED);
        assertThat(taskMapper.find(task.taskNo()).status())
            .isEqualTo(BmsExternalIntegrationApplicationService.SUCCEEDED);
    }

    @Test
    void invalidPaymentSignatureNeverClaimsReceipt() {
        var mapper = new BmsApplicationServiceTest.MemoryBmsMapper();
        mapper.refunds.put("RF-1", new BmsMapper.RefundSettlementRow(
            1L, "RF-1", "B-1", BigDecimal.TEN,
            BmsDomain.RefundSettlementAggregate.REQUESTED, null, 1));
        var callback = new PaymentCallbackApplicationService(
            input -> { throw new IllegalArgumentException("signature invalid"); },
            new BmsApplicationService(mapper));

        assertThatThrownBy(() -> callback.receive(
            new PaymentCallbackApplicationService.CallbackCommand(
                "RF-1", "PAY-EVT-1", true, null,
                1L, "nonce", "bad", "{}")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("signature invalid");
        assertThat(mapper.refundReceipts).isEmpty();
    }

    @Test
    void unknownPaymentResultKeepsRefundPendingInsteadOfReleasingAmount() {
        var mapper = new BmsApplicationServiceTest.MemoryBmsMapper();
        mapper.refunds.put("RF-UNKNOWN", new BmsMapper.RefundSettlementRow(
            1L, "RF-UNKNOWN", "B-1", BigDecimal.TEN,
            BmsDomain.RefundSettlementAggregate.REQUESTED, null, 1));
        var taskMapper = new MemoryTaskMapper();
        var service = integration(mapper, taskMapper,
            request -> new ErpFinanceGateway.PostingResult("VOUCHER-1"),
            request -> new TaxInvoiceGateway.IssueResult("TAX-1"),
            request -> { throw new PaymentGateway.ResultUnknownException("timeout", null); });

        service.enqueueRefund("RF-UNKNOWN", "refund-unknown-1");
        service.dispatch(10);

        assertThat(mapper.refunds.get("RF-UNKNOWN").status())
            .isEqualTo(BmsDomain.RefundSettlementAggregate.CONFIRMATION_PENDING);
        assertThat(mapper.occupiedRefundAmount("B-1")).isEqualByComparingTo("10.00");
    }

    @Test
    void manualRetryRequiresBillingObjectScopeAndWritesAudit() {
        var mapper = new BmsApplicationServiceTest.MemoryBmsMapper();
        mapper.bills.put("B-1", new BmsMapper.BillRow(
            1L, "B-1", "RC-1", "OBJ-A", new BigDecimal("100.00"), 2, 1));
        mapper.finances.put("FH-1", new BmsMapper.FinanceHandoverRow(
            1L, "FH-1", "B-1", BmsDomain.FinanceHandoverAggregate.FAILED,
            null, "ERP unavailable", 1));
        var taskMapper = new MemoryTaskMapper();
        taskMapper.rows.put("TASK-1", new BmsExternalTaskMapper.ExternalTaskRow(
            "TASK-1", "ERP_POST", "FH-1", "idem-1",
            BmsExternalIntegrationApplicationService.FINAL_FAILED,
            8, 8, null, "ERP unavailable", null, 2));
        var service = integration(mapper, taskMapper,
            request -> new ErpFinanceGateway.PostingResult("VOUCHER-1"),
            request -> new TaxInvoiceGateway.IssueResult("TAX-1"),
            request -> new PaymentGateway.RefundResult("PAY-1"));

        assertThatThrownBy(() -> service.retryFinalFailure(
            "TASK-1", "人工复核后重试", access("OBJ-B")))
            .isInstanceOf(BusinessException.class);
        service.retryFinalFailure("TASK-1", "人工复核后重试", access("OBJ-A"));

        assertThat(taskMapper.find("TASK-1").status())
            .isEqualTo(BmsExternalIntegrationApplicationService.PENDING);
        assertThat(taskMapper.lastAuditReason).isEqualTo("人工复核后重试");
    }

    private static ScmAccessContext access(String objectCode) {
        return new ScmAccessContext(1001, "finance", "BMS",
            Set.of("bms:external-task:retry"),
            Map.of("BILLING_OBJECT", Set.of(objectCode)));
    }

    private static BmsExternalIntegrationApplicationService integration(
            BmsApplicationServiceTest.MemoryBmsMapper mapper,
            BmsExternalTaskMapper tasks, ErpFinanceGateway erp,
            TaxInvoiceGateway tax, PaymentGateway payment) {
        return new BmsExternalIntegrationApplicationService(
            tasks, mapper, new BmsApplicationService(mapper), erp, tax, payment);
    }

    private static final class MemoryTaskMapper implements BmsExternalTaskMapper {
        private final Map<String, ExternalTaskRow> rows = new LinkedHashMap<>();
        private String lastAuditReason;

        @Override
        public ExternalTaskRow findByIdempotencyKey(String idempotencyKey) {
            return rows.values().stream()
                .filter(row -> row.idempotencyKey().equals(idempotencyKey))
                .findFirst().orElse(null);
        }

        @Override
        public ExternalTaskRow find(String taskNo) {
            return rows.get(taskNo);
        }

        @Override
        public int insert(ExternalTaskRow row) {
            rows.put(row.taskNo(), row);
            return 1;
        }

        @Override
        public List<ExternalTaskRow> claimable(int limit) {
            return rows.values().stream()
                .filter(row -> row.status()
                    == BmsExternalIntegrationApplicationService.PENDING
                    || row.status()
                    == BmsExternalIntegrationApplicationService.RETRY_WAITING)
                .limit(limit).toList();
        }

        @Override
        public int claim(String taskNo, long version) {
            ExternalTaskRow row = rows.get(taskNo);
            if (row == null || row.version() != version) {
                return 0;
            }
            rows.put(taskNo, copy(row,
                BmsExternalIntegrationApplicationService.PROCESSING,
                row.attemptCount() + 1, row.externalRef(), row.lastError(),
                row.version() + 1));
            return 1;
        }

        @Override
        public int markSucceeded(String taskNo, String externalRef) {
            ExternalTaskRow row = rows.get(taskNo);
            rows.put(taskNo, copy(row,
                BmsExternalIntegrationApplicationService.SUCCEEDED,
                row.attemptCount(), externalRef, null, row.version() + 1));
            return 1;
        }

        @Override
        public int markFailed(String taskNo, int status, String reason) {
            ExternalTaskRow row = rows.get(taskNo);
            rows.put(taskNo, copy(row, status, row.attemptCount(),
                row.externalRef(), reason, row.version() + 1));
            return 1;
        }

        @Override
        public int retryFinalFailure(String taskNo) {
            ExternalTaskRow row = rows.get(taskNo);
            if (row == null || row.status()
                    != BmsExternalIntegrationApplicationService.FINAL_FAILED) {
                return 0;
            }
            rows.put(taskNo, copy(row,
                BmsExternalIntegrationApplicationService.PENDING,
                0, row.externalRef(), null, row.version() + 1));
            return 1;
        }

        @Override
        public int insertRetryAudit(String taskNo, long operatorId, String reason) {
            lastAuditReason = reason;
            return 1;
        }

        private static ExternalTaskRow copy(ExternalTaskRow row, int status,
                                            int attempts, String externalRef,
                                            String error, long version) {
            return new ExternalTaskRow(row.taskNo(), row.taskType(),
                row.businessNo(), row.idempotencyKey(), status, attempts,
                row.maxAttempts(), null, error, externalRef, version);
        }
    }
}
