package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.purchase.application.orderchange.*;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase/v1/order-changes")
public class PurchaseOrderChangeController {
    private final PurchaseOrderChangeApplicationService applicationService;
    private final PurchaseOrderChangeQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public PurchaseOrderChangeController(PurchaseOrderChangeApplicationService applicationService,
                                         PurchaseOrderChangeQueryApplicationService queryService,
                                         CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<PurchaseOrderChangeView>> page(@RequestParam(required = false) String orderNo,
                                                                 @RequestParam(required = false) Integer status,
                                                                 @RequestParam(defaultValue = "1") int pageNo,
                                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                                 HttpServletRequest request) {
        return ok(queryService.page(orderNo, status, pageNo, pageSize), request);
    }

    @GetMapping("/{changeNo}")
    public ApiResponse<PurchaseOrderChangeView> detail(@PathVariable String changeNo, HttpServletRequest request) {
        return ok(queryService.detail(changeNo), request);
    }

    @PostMapping
    public ApiResponse<CommandResult> create(@Valid @RequestBody CreateRequest body, HttpServletRequest request,
                                             Authentication authentication) {
        return ok(applicationService.create(body.toCommand(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{changeNo}/approve")
    public ApiResponse<CommandResult> approve(@PathVariable String changeNo, @Valid @RequestBody ApproveRequest body,
                                              HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.approve(changeNo, new PurchaseOrderChangeCommands.Approve(body.version(),
                body.approved()), body.lineQtyChanges(), contexts.create(request, authentication)), request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record CreateRequest(@NotBlank String orderNo, @Min(1) @Max(5) int changeType,
                                @NotBlank String beforeSnapshot, @NotBlank String afterSnapshot,
                                @NotBlank String changeReason,
                                Map<@Positive Long, @DecimalMin(value = "0", inclusive = false) BigDecimal> lineQtyChanges) {
        PurchaseOrderChangeCommands.Create toCommand() {
            return new PurchaseOrderChangeCommands.Create(orderNo, changeType, beforeSnapshot, afterSnapshot,
                    changeReason, lineQtyChanges);
        }
    }

    public record ApproveRequest(@PositiveOrZero int version, boolean approved,
                                 Map<@Positive Long, @DecimalMin(value = "0", inclusive = false) BigDecimal> lineQtyChanges) {
    }
}
