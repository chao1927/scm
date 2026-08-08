package com.chaobo.scm.bms.application.integration;

import com.chaobo.scm.bms.application.BmsApplicationService;
import com.chaobo.scm.bms.domain.BmsDomain;
import com.chaobo.scm.bms.infrastructure.persistence.BmsExternalTaskMapper;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.logging.ScmLogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ERP、税控和支付集成的可靠任务编排服务。
 *
 * <p>外部调用在短事务之外执行；请求号同时作为外部幂等键。失败按退避重试，
 * 达到上限后进入最终失败，必须由有权限的人工重试接口恢复。
 *
 * @author SCM Team
 */
@Service
public class BmsExternalIntegrationApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(BmsExternalIntegrationApplicationService.class);

    public static final int PENDING = 1;
    public static final int SUCCEEDED = 2;
    public static final int RETRY_WAITING = 3;
    public static final int FINAL_FAILED = 4;
    public static final int PROCESSING = 6;
    private static final String ERP_POST = "ERP_POST";
    private static final String TAX_ISSUE = "TAX_ISSUE";
    private static final String PAYMENT_REFUND = "PAYMENT_REFUND";
    private static final String BILLING_OBJECT_SCOPE = "BILLING_OBJECT";
    private static final int MAX_DISPATCH_LIMIT = 100;

    private final BmsExternalTaskMapper tasks;
    private final BmsMapper bmsMapper;
    private final BmsApplicationService bms;
    private final ErpFinanceGateway erp;
    private final TaxInvoiceGateway tax;
    private final PaymentGateway payment;

    public BmsExternalIntegrationApplicationService(
            BmsExternalTaskMapper tasks, BmsMapper bmsMapper,
            BmsApplicationService bms, ErpFinanceGateway erp,
            TaxInvoiceGateway tax, PaymentGateway payment) {
        this.tasks = tasks;
        this.bmsMapper = bmsMapper;
        this.bms = bms;
        this.erp = erp;
        this.tax = tax;
        this.payment = payment;
    }

    public BmsExternalTaskMapper.ExternalTaskRow enqueueErpPosting(
            String handoverNo, String idempotencyKey) {
        require(bmsMapper.findFinanceHandover(handoverNo), "finance handover not found");
        return enqueue(ERP_POST, handoverNo, idempotencyKey, 8);
    }

    public BmsExternalTaskMapper.ExternalTaskRow enqueueInvoiceIssue(
            String invoiceNo, String idempotencyKey) {
        require(bmsMapper.findInvoice(invoiceNo), "invoice not found");
        return enqueue(TAX_ISSUE, invoiceNo, idempotencyKey, 8);
    }

    public BmsExternalTaskMapper.ExternalTaskRow enqueueRefund(
            String refundNo, String idempotencyKey) {
        require(bmsMapper.findRefundSettlement(refundNo), "refund not found");
        return enqueue(PAYMENT_REFUND, refundNo, idempotencyKey, 8);
    }

    @Transactional(rollbackFor = Exception.class)
    public BmsExternalTaskMapper.ExternalTaskRow enqueue(
            String taskType, String businessNo, String idempotencyKey, int maxAttempts) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("external task idempotency key is required");
        }
        var existing = tasks.findByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            if (!existing.taskType().equals(taskType)
                    || !existing.businessNo().equals(businessNo)) {
                throw new IllegalStateException(
                    "external task idempotency key conflicts with another request");
            }
            return existing;
        }
        String taskNo = "BEXT-" + UUID.randomUUID();
        tasks.insert(new BmsExternalTaskMapper.ExternalTaskRow(
            taskNo, taskType, businessNo, idempotencyKey, PENDING, 0,
            Math.max(1, maxAttempts), null, null, null, 1));
        return tasks.find(taskNo);
    }

    /**
     * 调度一批外部任务。
     */
    public DispatchResult dispatch(int requestedLimit) {
        int succeeded = 0;
        int failed = 0;
        for (var candidate : tasks.claimable(
                Math.max(1, Math.min(requestedLimit, MAX_DISPATCH_LIMIT)))) {
            if (tasks.claim(candidate.taskNo(), candidate.version()) == 0) {
                continue;
            }
            var claimed = tasks.find(candidate.taskNo());
            try (ScmLogContext ignored = ScmLogContext.openSystem(claimed.taskNo())) {
                String externalRef = execute(claimed);
                tasks.markSucceeded(claimed.taskNo(), externalRef);
                succeeded++;
                LOG.info("event=integration_command operation=bms_external_dispatch result=SUCCESS taskNo={} taskType={}",
                        claimed.taskNo(), claimed.taskType());
            } catch (PaymentGateway.ResultUnknownException exception) {
                boolean terminal = claimed.attemptCount() >= claimed.maxAttempts();
                markRefundResultUnknown(claimed, message(exception));
                tasks.markFailed(claimed.taskNo(),
                    terminal ? FINAL_FAILED : RETRY_WAITING, message(exception));
                failed++;
                try (ScmLogContext ignored = ScmLogContext.openSystem(claimed.taskNo())) {
                    LOG.error("event=integration_command operation=bms_external_dispatch result=UNKNOWN taskNo={} taskType={} terminal={}",
                            claimed.taskNo(), claimed.taskType(), terminal, exception);
                }
            } catch (RuntimeException exception) {
                boolean terminal = claimed.attemptCount() >= claimed.maxAttempts();
                if (terminal) {
                    markBusinessFailure(claimed, message(exception));
                }
                tasks.markFailed(claimed.taskNo(),
                    terminal ? FINAL_FAILED : RETRY_WAITING, message(exception));
                failed++;
                try (ScmLogContext ignored = ScmLogContext.openSystem(claimed.taskNo())) {
                    LOG.error("event=integration_command operation=bms_external_dispatch result=FAILURE taskNo={} taskType={} terminal={}",
                            claimed.taskNo(), claimed.taskType(), terminal, exception);
                }
            }
        }
        return new DispatchResult(succeeded, failed);
    }

    /**
     * 人工恢复最终失败任务。
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryFinalFailure(String taskNo, String reason, ScmAccessContext access) {
        var task = require(tasks.find(taskNo), "external task not found");
        access.requireScope(BILLING_OBJECT_SCOPE, billingObjectCode(task));
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("manual retry reason is required");
        }
        if (reason.trim().length() > 128) {
            throw new IllegalArgumentException("manual retry reason is too long");
        }
        if (tasks.retryFinalFailure(taskNo) == 0) {
            throw new IllegalStateException("only final failed task can be retried");
        }
        tasks.insertRetryAudit(taskNo, access.operatorId(), reason.trim());
        task = tasks.find(taskNo);
        if (PAYMENT_REFUND.equals(task.taskType())) {
            var refund = require(bmsMapper.findRefundSettlement(task.businessNo()),
                "refund not found");
            if (refund.status() == BmsDomain.RefundSettlementAggregate.FAILED) {
                bms.retryRefundSettlement(refund.refundNo(),
                    new BmsApplicationService.VersionCommand(
                        refund.version(), null, taskNo + ":manual-retry"));
            }
        }
    }

    private String billingObjectCode(BmsExternalTaskMapper.ExternalTaskRow task) {
        String billNo = switch (task.taskType()) {
            case ERP_POST -> require(
                bmsMapper.findFinanceHandover(task.businessNo()),
                "finance handover not found").billNo();
            case TAX_ISSUE -> require(
                bmsMapper.findInvoice(task.businessNo()), "invoice not found").billNo();
            case PAYMENT_REFUND -> require(
                bmsMapper.findRefundSettlement(task.businessNo()),
                "refund not found").billNo();
            default -> throw new IllegalArgumentException(
                "unsupported BMS external task: " + task.taskType());
        };
        return require(bmsMapper.findBill(billNo), "bill not found").objectCode();
    }

    private String execute(BmsExternalTaskMapper.ExternalTaskRow task) {
        return switch (task.taskType()) {
            case ERP_POST -> postErp(task);
            case TAX_ISSUE -> issueInvoice(task);
            case PAYMENT_REFUND -> requestRefund(task);
            default -> throw new IllegalArgumentException(
                "unsupported BMS external task: " + task.taskType());
        };
    }

    private String postErp(BmsExternalTaskMapper.ExternalTaskRow task) {
        var handover = require(bmsMapper.findFinanceHandover(task.businessNo()),
            "finance handover not found");
        var bill = require(bmsMapper.findBill(handover.billNo()), "bill not found");
        if (handover.status() == BmsDomain.FinanceHandoverAggregate.POSTED) {
            return handover.voucherNo();
        }
        var result = erp.post(new ErpFinanceGateway.PostingRequest(
            task.taskNo(), handover.handoverNo(), bill.billNo(), bill.totalAmount(), "CNY"));
        bms.postFinanceHandover(handover.handoverNo(),
            new BmsApplicationService.PostFinanceCommand(
                result.voucherNo(), handover.version(), null, task.taskNo()));
        return result.voucherNo();
    }

    private String issueInvoice(BmsExternalTaskMapper.ExternalTaskRow task) {
        var invoice = require(bmsMapper.findInvoice(task.businessNo()), "invoice not found");
        var bill = require(bmsMapper.findBill(invoice.billNo()), "bill not found");
        if (invoice.status() == BmsDomain.InvoiceAggregate.ISSUED) {
            return invoice.invoiceNo();
        }
        var result = tax.issue(new TaxInvoiceGateway.IssueRequest(
            task.taskNo(), invoice.invoiceNo(), bill.billNo(),
            invoice.invoiceAmount(), "CNY"));
        bms.issueInvoice(invoice.invoiceNo(), new BmsApplicationService.VersionCommand(
            invoice.version(), null, task.taskNo()));
        return result.externalInvoiceNo();
    }

    private String requestRefund(BmsExternalTaskMapper.ExternalTaskRow task) {
        var refund = require(bmsMapper.findRefundSettlement(task.businessNo()),
            "refund not found");
        var result = payment.refund(new PaymentGateway.RefundRequest(
            task.taskNo(), refund.refundNo(), refund.billNo(),
            refund.refundAmount(), refund.currency()));
        return result.paymentRequestNo();
    }

    private void markRefundResultUnknown(BmsExternalTaskMapper.ExternalTaskRow task,
                                         String reason) {
        if (!PAYMENT_REFUND.equals(task.taskType())) {
            return;
        }
        var row = bmsMapper.findRefundSettlement(task.businessNo());
        if (row != null && row.status() == BmsDomain.RefundSettlementAggregate.REQUESTED) {
            bms.markRefundConfirmationPending(row.refundNo(),
                new BmsApplicationService.ConfirmationPendingCommand(
                    reason, row.version(), null, task.taskNo() + ":unknown"));
        }
    }

    private void markBusinessFailure(BmsExternalTaskMapper.ExternalTaskRow task,
                                     String reason) {
        if (ERP_POST.equals(task.taskType())) {
            var row = bmsMapper.findFinanceHandover(task.businessNo());
            if (row != null && row.status() != BmsDomain.FinanceHandoverAggregate.POSTED) {
                bms.failFinanceHandover(row.handoverNo(),
                    new BmsApplicationService.FailCommand(
                        reason, row.version(), null, task.taskNo()));
            }
        } else if (PAYMENT_REFUND.equals(task.taskType())) {
            var row = bmsMapper.findRefundSettlement(task.businessNo());
            if (row != null && row.status() != BmsDomain.RefundSettlementAggregate.FINISHED) {
                bms.failRefundSettlement(row.refundNo(),
                    new BmsApplicationService.FailCommand(
                        reason, row.version(), null, task.taskNo()));
            }
        }
    }

    private static String message(RuntimeException exception) {
        String value = exception.getMessage();
        if (value == null || value.isBlank()) {
            value = exception.getClass().getSimpleName();
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public record DispatchResult(int succeeded, int failed) {
    }
}
