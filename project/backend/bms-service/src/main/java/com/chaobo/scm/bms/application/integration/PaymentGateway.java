package com.chaobo.scm.bms.application.integration;

import java.math.BigDecimal;

/**
 * 支付退款防腐端口。
 *
 * @author SCM Team
 */
public interface PaymentGateway {

    /**
     * 向支付系统提交幂等退款请求。
     *
     * @param request 退款请求
     * @return 支付请求结果
     */
    RefundResult refund(RefundRequest request);

    record RefundRequest(String requestId, String refundNo, String billNo,
                         BigDecimal amount, String currency) {
    }

    record RefundResult(String paymentRequestNo) {
    }

    /**
     * 请求已发出但无法确认渠道是否受理时的专用异常。
     * 调用方不得将它当作明确失败释放退款额度。
     */
    final class ResultUnknownException extends RuntimeException {

        public ResultUnknownException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
