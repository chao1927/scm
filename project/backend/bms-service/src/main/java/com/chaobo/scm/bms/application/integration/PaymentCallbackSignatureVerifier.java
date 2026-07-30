package com.chaobo.scm.bms.application.integration;

/**
 * 支付回调验签端口。
 *
 * @author SCM Team
 */
public interface PaymentCallbackSignatureVerifier {

    /**
     * 校验回调时间戳、随机数、原始报文和签名。
     *
     * @param input 签名输入
     */
    void verify(SignatureInput input);

    record SignatureInput(long timestamp, String nonce, String rawBody, String signature) {
    }
}
