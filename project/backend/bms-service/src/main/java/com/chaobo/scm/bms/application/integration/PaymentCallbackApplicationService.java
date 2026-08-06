package com.chaobo.scm.bms.application.integration;

import com.chaobo.scm.bms.application.BmsApplicationService;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 支付退款回调应用服务。
 *
 * <p>验签严格先于回执幂等声明；相同支付回执不会重复推进退款聚合。
 *
 * @author SCM Team
 */
@Service
public class PaymentCallbackApplicationService {

    private final PaymentCallbackSignatureVerifier verifier;
    private final BmsApplicationService bms;

    public PaymentCallbackApplicationService(PaymentCallbackSignatureVerifier verifier,
                                             BmsApplicationService bms) {
        this.verifier = verifier;
        this.bms = bms;
    }

    public BmsMapper.RefundSettlementRow receive(CallbackCommand command) {
        verifier.verify(new PaymentCallbackSignatureVerifier.SignatureInput(
            command.timestamp(), command.nonce(), command.rawBody(), command.signature()));
        return bms.consumeRefundReceipt(command.refundNo(),
            new BmsApplicationService.RefundReceiptCommand(
                command.receiptNo(), command.success(), command.failureReason(),
                command.refundAmount(), command.currency(), command.merchantNo(),
                command.paymentTxnNo(), command.rawBody()));
    }

    public record CallbackCommand(String refundNo, String receiptNo, boolean success,
                                  String failureReason, BigDecimal refundAmount, String currency,
                                  String merchantNo, String paymentTxnNo, long timestamp,
                                  String nonce, String signature, String rawBody) {

        /** 兼容验签失败等尚未进入业务校验的历史用例。 */
        public CallbackCommand(String refundNo, String receiptNo, boolean success,
                               String failureReason, long timestamp, String nonce,
                               String signature, String rawBody) {
            this(refundNo, receiptNo, success, failureReason, null, null, null,
                null, timestamp, nonce, signature, rawBody);
        }
    }
}
