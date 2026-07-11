package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.supplier.application.report.SupplierReportApplicationService;
import com.chaobo.scm.supplier.application.report.SupplierReportViews;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier/v1/reports")
public class SupplierReportController {
    private final SupplierReportApplicationService service;

    public SupplierReportController(SupplierReportApplicationService service) {
        this.service = service;
    }

    @GetMapping("/fulfillment")
    public ApiResponse<SupplierReportViews.Fulfillment> fulfillment(@RequestParam(required = false) Long supplierId,
                                                                    @AuthenticationPrincipal Jwt jwt,
                                                                    HttpServletRequest request) {
        return ok(service.fulfillment(supplierId, scope(jwt)), request);
    }

    @GetMapping("/exceptions")
    public ApiResponse<SupplierReportViews.ExceptionOverview> exceptions(@RequestParam(required = false) Long supplierId,
                                                                        @AuthenticationPrincipal Jwt jwt,
                                                                        HttpServletRequest request) {
        return ok(service.exceptions(supplierId, scope(jwt)), request);
    }

    private <T> ApiResponse<T> ok(T value, HttpServletRequest request) {
        return ApiResponse.success(value, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private Long scope(Jwt jwt) {
        if (jwt == null || !jwt.hasClaim("supplier_id")) {
            return null;
        }
        Number value = jwt.getClaim("supplier_id");
        return value == null ? null : value.longValue();
    }
}
