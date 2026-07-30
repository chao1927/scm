package com.chaobo.scm.bms.application.integration;

import java.math.BigDecimal;

/**
 * ERP 财务过账防腐端口。
 *
 * <p>端口使用 BMS 自有语义，外部 ERP DTO 只能存在于基础设施适配器中。
 *
 * @author SCM Team
 */
public interface ErpFinanceGateway {

    /**
     * 将财务交接单可靠过账到 ERP。
     *
     * @param request 过账请求
     * @return ERP 凭证结果
     */
    PostingResult post(PostingRequest request);

    record PostingRequest(String requestId, String handoverNo, String billNo,
                          BigDecimal amount, String currency) {
    }

    record PostingResult(String voucherNo) {
    }
}
