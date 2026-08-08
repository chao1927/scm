package com.chaobo.scm.bms.interfaces.web;

import com.chaobo.scm.bms.application.integration.BmsExternalIntegrationApplicationService;
import com.chaobo.scm.bms.application.integration.PaymentCallbackApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsExternalTaskMapper;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BMS 财税支付集成命令与支付回调接口。
 *
 * @author SCM Team
 */
@RestController
@RequestMapping("/api/bms/v1")
public class BmsExternalIntegrationController {

    private final BmsExternalIntegrationApplicationService integration;
    private final PaymentCallbackApplicationService callback;
    private final ObjectMapper objectMapper;

    public BmsExternalIntegrationController(
            BmsExternalIntegrationApplicationService integration,
            PaymentCallbackApplicationService callback,
            ObjectMapper objectMapper) {
        this.integration = integration;
        this.callback = callback;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/finance-handovers/{handoverNo}/erp-post")
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:external-task:create')")
    public BmsExternalTaskMapper.ExternalTaskRow enqueueErp(
            @PathVariable String handoverNo,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        return integration.enqueueErpPosting(handoverNo, idempotencyKey);
    }

    @PostMapping("/invoices/{invoiceNo}/tax-issue")
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:external-task:create')")
    public BmsExternalTaskMapper.ExternalTaskRow enqueueInvoice(
            @PathVariable String invoiceNo,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        return integration.enqueueInvoiceIssue(invoiceNo, idempotencyKey);
    }

    @PostMapping("/refund-settlements/{refundNo}/payment")
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:external-task:create')")
    public BmsExternalTaskMapper.ExternalTaskRow enqueueRefund(
            @PathVariable String refundNo,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        return integration.enqueueRefund(refundNo, idempotencyKey);
    }

    @PostMapping("/external-tasks/{taskNo}/retry")
    @PreAuthorize("hasAnyAuthority('*','bms:*','bms:external-task:retry')")
    public void retry(@PathVariable String taskNo, @RequestBody ManualRetryRequest request,
                      Authentication authentication) {
        integration.retryFinalFailure(taskNo, request.reason(),
            ScmAccessContexts.require(authentication));
    }

    @PostMapping("/payment-callbacks/refunds")
    public void paymentCallback(
            @RequestHeader("X-Timestamp") long timestamp,
            @RequestHeader("X-Nonce") String nonce,
            @RequestHeader("X-Signature") String signature,
            @RequestBody String rawBody) {
        PaymentCallbackRequest request = parse(rawBody);
        callback.receive(new PaymentCallbackApplicationService.CallbackCommand(
            request.refundNo(), request.receiptNo(), request.success(),
            request.failureReason(), request.refundAmount(), request.currency(),
            request.merchantNo(), request.paymentTxnNo(), timestamp, nonce, signature, rawBody));
    }

    private PaymentCallbackRequest parse(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, PaymentCallbackRequest.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("invalid payment callback body", exception);
        }
    }

    public record PaymentCallbackRequest(String refundNo, String receiptNo,
                                         boolean success, String failureReason,
                                         java.math.BigDecimal refundAmount, String currency,
                                         String merchantNo, String paymentTxnNo) {
    }

    public record ManualRetryRequest(String reason) {
    }
}
