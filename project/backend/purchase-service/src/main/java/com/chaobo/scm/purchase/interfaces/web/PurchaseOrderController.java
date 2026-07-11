package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.*;
import com.chaobo.scm.purchase.application.order.*;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase/v1/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderApplicationService applicationService;
    private final PurchaseOrderQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public PurchaseOrderController(PurchaseOrderApplicationService applicationService,
                                   PurchaseOrderQueryApplicationService queryService,
                                   CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<PurchaseOrderView>> page(@RequestParam(required = false) Long purchaseOrgId,
                                                           @RequestParam(required = false) Long supplierId,
                                                           @RequestParam(required = false) Integer status,
                                                           @RequestParam(defaultValue = "1") int pageNo,
                                                           @RequestParam(defaultValue = "20") int pageSize,
                                                           HttpServletRequest request) {
        return ok(queryService.page(purchaseOrgId, scope(request), supplierId, status, pageNo, pageSize), request);
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<PurchaseOrderView> detail(@PathVariable String orderNo, HttpServletRequest request) {
        return ok(queryService.detail(orderNo, scope(request)), request);
    }

    @PostMapping
    public ApiResponse<CommandResult> create(@Valid @RequestBody CreateRequest body, HttpServletRequest request,
                                             Authentication authentication) {
        return ok(applicationService.create(body.toCommand(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{orderNo}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable String orderNo, @Valid @RequestBody VersionRequest body,
                                             HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.submit(orderNo, new PurchaseOrderCommands.Version(body.version()),
                contexts.create(request, authentication)), request);
    }

    @PostMapping("/{orderNo}/approve")
    public ApiResponse<CommandResult> approve(@PathVariable String orderNo, @Valid @RequestBody ApproveRequest body,
                                              HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.approve(orderNo, new PurchaseOrderCommands.Approve(body.version(),
                body.approved(), body.reason()), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{orderNo}/publish")
    public ApiResponse<CommandResult> publish(@PathVariable String orderNo, @Valid @RequestBody PublishRequest body,
                                              HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.publish(orderNo, new PurchaseOrderCommands.Publish(body.version(),
                body.publishMode()), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<CommandResult> cancel(@PathVariable String orderNo, @Valid @RequestBody CancelRequest body,
                                             HttpServletRequest request, Authentication authentication) {
        return ok(applicationService.cancel(orderNo, new PurchaseOrderCommands.Cancel(body.version(),
                body.reason()), contexts.create(request, authentication)), request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private Long scope(HttpServletRequest request) {
        var value = request.getHeader("X-Purchase-Org-Id");
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    public record CreateRequest(@Min(1) @Max(4) int purchaseType, @Positive long supplierId,
                                @NotBlank String supplierCode, @NotBlank String supplierName,
                                @Positive long purchaseOrgId, String warehouseCode, @NotBlank String currency,
                                @NotEmpty List<@Valid LineRequest> lines) {
        PurchaseOrderCommands.Create toCommand() {
            return new PurchaseOrderCommands.Create(purchaseType, supplierId, supplierCode, supplierName,
                    purchaseOrgId, warehouseCode, currency, lines.stream().map(LineRequest::toCommand).toList());
        }
    }

    public record LineRequest(Long lineId, @NotBlank String skuCode, String skuName,
                              @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal orderQty,
                              @NotNull @DecimalMin("0") BigDecimal unitPrice,
                              @NotNull @DecimalMin("0") BigDecimal taxRate,
                              LocalDate requiredDeliveryDate) {
        PurchaseOrderCommands.Line toCommand() {
            return new PurchaseOrderCommands.Line(lineId, skuCode, skuName, orderQty, unitPrice, taxRate,
                    requiredDeliveryDate);
        }
    }

    public record VersionRequest(@PositiveOrZero int version) {
    }

    public record ApproveRequest(@PositiveOrZero int version, boolean approved, String reason) {
    }

    public record PublishRequest(@PositiveOrZero int version, String publishMode) {
    }

    public record CancelRequest(@PositiveOrZero int version, @NotBlank String reason) {
    }
}
