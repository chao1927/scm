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
}
