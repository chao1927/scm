package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.supplier.application.workbench.SupplierWorkbenchApplicationService;
import com.chaobo.scm.supplier.application.workbench.SupplierWorkbenchView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier/v1/workbench")
public class SupplierWorkbenchController {
    private final SupplierWorkbenchApplicationService service;

    public SupplierWorkbenchController(SupplierWorkbenchApplicationService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ApiResponse<SupplierWorkbenchView> summary(@RequestParam(required = false) Long supplierId,
                                                      @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days,
                                                      @AuthenticationPrincipal Jwt jwt,
                                                      HttpServletRequest request) {
        return ApiResponse.success(service.summary(supplierId, scope(jwt), days),
                request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private Long scope(Jwt jwt) {
        if (jwt == null || !jwt.hasClaim("supplier_id")) {
            return null;
        }
        Number value = jwt.getClaim("supplier_id");
        return value == null ? null : value.longValue();
    }
}
