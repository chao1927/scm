package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.supplierconfirm.SupplierConfirmApplicationService;
import com.chaobo.scm.purchase.application.supplierconfirm.SupplierConfirmCommands;
import com.chaobo.scm.purchase.application.supplierconfirm.SupplierConfirmView;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase/v1/supplier-confirms")
public class SupplierConfirmController {
    private final SupplierConfirmApplicationService service;
    private final CommandContextFactory contexts;

    public SupplierConfirmController(SupplierConfirmApplicationService service, CommandContextFactory contexts) {
        this.service = service;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<SupplierConfirmView>> page(@RequestParam(required = false) Long purchaseOrgId,
                                                             @RequestParam(required = false) String orderNo,
                                                             @RequestParam(required = false) Long supplierId,
                                                             @RequestParam(required = false) Integer processedStatus,
                                                             @RequestParam(defaultValue = "1") int pageNo,
                                                             @RequestParam(defaultValue = "20") int pageSize,
                                                             HttpServletRequest request,
                                                             Authentication authentication) {
        var context = contexts.create(request, authentication);
        return ok(service.page(purchaseOrgId, context.purchaseOrgScope(), orderNo, supplierId, processedStatus,
                pageNo, pageSize, context), request);
    }

    @GetMapping("/{confirmId}")
    public ApiResponse<SupplierConfirmView> detail(@PathVariable long confirmId, HttpServletRequest request,
                                                   Authentication authentication) {
        var context = contexts.create(request, authentication);
        return ok(service.detail(confirmId, context.purchaseOrgScope(), context), request);
    }

    @PostMapping("/{confirmId}/accept-diff")
    public ApiResponse<Void> acceptDifference(@PathVariable long confirmId, @Valid @RequestBody ProcessRequest body,
                                              HttpServletRequest request, Authentication authentication) {
        service.acceptDifference(confirmId, new SupplierConfirmCommands.Process(body.version(), body.comment()),
                contexts.create(request, authentication));
        return ok(null, request);
    }

    @PostMapping("/{confirmId}/renegotiate")
    public ApiResponse<Void> renegotiate(@PathVariable long confirmId, @Valid @RequestBody RenegotiateRequest body,
                                         HttpServletRequest request, Authentication authentication) {
        service.renegotiate(confirmId, new SupplierConfirmCommands.Renegotiate(body.version(), body.requirement(),
                body.comment()), contexts.create(request, authentication));
        return ok(null, request);
    }

    @PostMapping("/{confirmId}/cancel-order")
    public ApiResponse<Void> cancelOrder(@PathVariable long confirmId, @Valid @RequestBody CancelRequest body,
                                         HttpServletRequest request, Authentication authentication) {
        service.cancelOrder(confirmId, new SupplierConfirmCommands.CancelOrder(body.version(), body.reason()),
                contexts.create(request, authentication));
        return ok(null, request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record ProcessRequest(@PositiveOrZero int version, String comment) {
    }

    public record RenegotiateRequest(@PositiveOrZero int version, @NotBlank String requirement, String comment) {
    }

    public record CancelRequest(@PositiveOrZero int version, @NotBlank String reason) {
    }
}
