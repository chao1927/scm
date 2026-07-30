package com.chaobo.scm.tms.application;

/**
 * 承运商回调签名验证端口。
 *
 * <p>应用层只依赖“回调是否可信”的业务语义，具体密钥读取、HMAC 算法和时钟窗口由基础设施实现。
 *
 * @author SCM Team
 */
public interface CarrierCallbackSignatureVerifier {

    /**
     * 验证承运商原始回调报文。
     *
     * @param input 未经改写的签名输入
     * @throws IllegalArgumentException 签名字段缺失、过期或签名不匹配
     */
    void verify(SignatureInput input);

    /**
     * 签名输入。时间戳使用 Unix 秒，正文必须是 HTTP 接收到的原始 JSON。
     */
    record SignatureInput(String carrierCode, long timestamp, String nonce,
                          String rawBody, String signature) {
    }
}
