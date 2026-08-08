package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.purchase.application.operations.PurchaseOperationsApplicationService;
import com.chaobo.scm.purchase.application.operations.PurchaseOperationsViews;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** 采购上下文供应商报价事实查询接口。 */
@RestController
@RequestMapping("/api/purchase/v1/quotations")
public class PurchaseQuotationQueryController {

    private final PurchaseOperationsApplicationService operations;

    public PurchaseQuotationQueryController(PurchaseOperationsApplicationService operations) {
        this.operations = operations;
    }

    /** 查询采购上下文通过 RocketMQ 接收并持久化的供应商报价事实。 */
    @GetMapping
    public ApiResponse<List<PurchaseOperationsViews.Quotation>> quotations(HttpServletRequest request) {
        return ApiResponse.success(operations.quotations(), request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }
}
