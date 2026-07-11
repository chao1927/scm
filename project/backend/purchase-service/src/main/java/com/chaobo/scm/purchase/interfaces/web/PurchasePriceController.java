package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.price.PurchasePriceApplicationService;
import com.chaobo.scm.purchase.application.price.PurchasePriceCommands;
import com.chaobo.scm.purchase.application.price.PurchasePriceQueryApplicationService;
import com.chaobo.scm.purchase.application.price.PurchasePriceView;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
import java.time.LocalDate;

@RestController
@RequestMapping("/api/purchase/v1/purchase-prices")
public class PurchasePriceController {
    private final PurchasePriceApplicationService applicationService;
    private final PurchasePriceQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public PurchasePriceController(
            PurchasePriceApplicationService applicationService,
            PurchasePriceQueryApplicationService queryService,
            CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<PurchasePriceView>> page(
            @RequestParam(required = false) Long purchaseOrgId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String skuCode,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        return ok(queryService.page(
                purchaseOrgId,
                optionalLong(request.getHeader("X-Purchase-Org-Id")),
                supplierId,
                skuCode,
                currency,
                status,
                pageNo,
                pageSize), request);
    }

    @GetMapping("/{priceNo}")
    public ApiResponse<PurchasePriceView> detail(@PathVariable String priceNo, HttpServletRequest request) {
        return ok(queryService.detail(priceNo, optionalLong(request.getHeader("X-Purchase-Org-Id"))), request);
    }

    @PostMapping
    public ApiResponse<CommandResult> create(
            @Valid @RequestBody CreateRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(applicationService.create(body.toCommand(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{priceNo}/disable")
    public ApiResponse<CommandResult> disable(
            @PathVariable String priceNo,
            @Valid @RequestBody VersionRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(
                applicationService.disable(priceNo, new PurchasePriceCommands.Version(body.version()),
                        contexts.create(request, authentication)),
                request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private static Long optionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    public record CreateRequest(
            @Positive long supplierId,
            @NotBlank String skuCode,
            @Positive long purchaseOrgId,
            @Min(1) @Max(3) int priceType,
            @NotBlank String currency,
            @NotNull @DecimalMin("0") BigDecimal unitPrice,
            @NotNull @DecimalMin("0") BigDecimal taxRate,
            @NotNull LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String sourceType,
            String sourceNo) {

        PurchasePriceCommands.Create toCommand() {
            return new PurchasePriceCommands.Create(
                    supplierId,
                    skuCode,
                    purchaseOrgId,
                    priceType,
                    currency,
                    unitPrice,
                    taxRate,
                    effectiveFrom,
                    effectiveTo,
                    sourceType,
                    sourceNo);
        }
    }

    public record VersionRequest(@PositiveOrZero int version) {
    }
}
