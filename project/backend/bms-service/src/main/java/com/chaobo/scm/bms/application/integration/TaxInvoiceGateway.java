package com.chaobo.scm.bms.application.integration;

import java.math.BigDecimal;

/**
 * 税控开票防腐端口。
 *
 * @author SCM Team
 */
public interface TaxInvoiceGateway {

    /**
     * 向税控系统提交幂等开票请求。
     *
     * @param request 开票请求
     * @return 外部发票结果
     */
    IssueResult issue(IssueRequest request);

    record IssueRequest(String requestId, String invoiceNo, String billNo,
                        BigDecimal amount, String currency) {
    }

    record IssueResult(String externalInvoiceNo) {
    }
}
