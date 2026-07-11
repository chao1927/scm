package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnApplicationService;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnCommands;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnQueryApplicationService;
import com.chaobo.scm.purchase.application.supplierreturn.SupplierReturnView;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/purchase/v1/supplier-returns")
public class SupplierReturnController {
    private final SupplierReturnApplicationService applicationService;
    private final SupplierReturnQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public SupplierReturnController(SupplierReturnApplicationService applicationService,
                                    SupplierReturnQueryApplicationService queryService,
                                    CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<SupplierReturnView>> page(@RequestParam(required = false) Long purchaseOrgId,
                                                            @RequestParam(required = false) Long supplierId,
                                                            @RequestParam(required = false) String warehouseCode,
                                                            @RequestParam(required = false) Integer status,
                                                            @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "20") int pageSize,
                                                            HttpServletRequest request) {
        return ok(queryService.page(purchaseOrgId, scope(request), supplierId, warehouseCode, status,
                pageNo, pageSize), request);
    }

    @GetMapping("/{returnNo}")
    public ApiResponse<SupplierReturnView> detail(@PathVariable String returnNo, HttpServletRequest request) {
        return ok(queryService.detail(returnNo, scope(request)), request);
    }

    @PostMapping
    public ApiResponse<CommandResult> create(@Valid @RequestBody CreateRequest body,
                                             HttpServletRequest request,
                                             Authentication authentication) {
        return ok(applicationService.create(body.toCommand(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{returnNo}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable String returnNo,
                                             @Valid @RequestBody VersionRequest body,
                                             HttpServletRequest request,
                                             Authentication authentication) {
        return ok(applicationService.submit(returnNo, new SupplierReturnCommands.Version(body.version()),
                contexts.create(request, authentication)), request);
    }

    @PostMapping("/{returnNo}/approve")
    public ApiResponse<CommandResult> approve(@PathVariable String returnNo,
                                              @Valid @RequestBody ApproveRequest body,
                                              HttpServletRequest request,
                                              Authentication authentication) {
        return ok(applicationService.approve(returnNo, new SupplierReturnCommands.Approve(body.version(),
                body.approved(), body.reason()), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{returnNo}/notify-execution")
    public ApiResponse<CommandResult> notifyExecution(@PathVariable String returnNo,
                                                      @Valid @RequestBody NotifyRequest body,
                                                      HttpServletRequest request,
                                                      Authentication authentication) {
        return ok(applicationService.notifyExecution(returnNo, new SupplierReturnCommands.Notify(body.version(),
                body.notifyMode()), contexts.create(request, authentication)), request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private Long scope(HttpServletRequest request) {
        var value = request.getHeader("X-Purchase-Org-Id");
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    public record CreateRequest(@NotBlank String sourceOrderNo,
                                @Positive long supplierId,
                                @Positive long purchaseOrgId,
                                String warehouseCode,
                                @NotEmpty List<@Valid LineRequest> lines) {
        SupplierReturnCommands.Create toCommand() {
            return new SupplierReturnCommands.Create(sourceOrderNo, supplierId, purchaseOrgId, warehouseCode,
                    lines.stream().map(LineRequest::toCommand).toList());
        }
    }

    public record LineRequest(Long lineId,
                              @NotBlank String skuCode,
                              @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal returnQty,
                              @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal returnableQty,
                              String reason) {
        SupplierReturnCommands.Line toCommand() {
            return new SupplierReturnCommands.Line(lineId, skuCode, returnQty, returnableQty, reason);
        }
    }

    public record VersionRequest(@PositiveOrZero int version) {
    }

    public record ApproveRequest(@PositiveOrZero int version, boolean approved, String reason) {
    }

    public record NotifyRequest(@PositiveOrZero int version, String notifyMode) {
    }
}
