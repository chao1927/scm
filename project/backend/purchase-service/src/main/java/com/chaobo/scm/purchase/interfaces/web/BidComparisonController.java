package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.comparison.BidComparisonApplicationService;
import com.chaobo.scm.purchase.application.comparison.BidComparisonCommands;
import com.chaobo.scm.purchase.application.comparison.BidComparisonQueryApplicationService;
import com.chaobo.scm.purchase.application.comparison.BidComparisonView;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase/v1/bid-comparisons")
public class BidComparisonController {
    private final BidComparisonApplicationService applicationService;
    private final BidComparisonQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public BidComparisonController(
            BidComparisonApplicationService applicationService,
            BidComparisonQueryApplicationService queryService,
            CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<BidComparisonView>> page(
            @RequestParam(required = false) Long purchaseOrgId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String rfqNo,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        return ok(queryService.page(
                purchaseOrgId,
                optionalLong(request.getHeader("X-Purchase-Org-Id")),
                status,
                rfqNo,
                pageNo,
                pageSize), request);
    }

    @GetMapping("/{compareNo}")
    public ApiResponse<BidComparisonView> detail(@PathVariable String compareNo, HttpServletRequest request) {
        return ok(queryService.detail(compareNo, optionalLong(request.getHeader("X-Purchase-Org-Id"))), request);
    }

    @PostMapping
    public ApiResponse<CommandResult> generate(
            @Valid @RequestBody GenerateRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(applicationService.generate(body.toCommand(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{compareNo}/award")
    public ApiResponse<CommandResult> award(
            @PathVariable String compareNo,
            @Valid @RequestBody AwardRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(applicationService.award(compareNo, body.toCommand(), contexts.create(request, authentication)), request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private static Long optionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    public record GenerateRequest(
            @NotBlank String rfqNo,
            @Positive long purchaseOrgId,
            @NotBlank String currency,
            @NotEmpty List<@Valid CandidateRequest> candidates) {

        BidComparisonCommands.Generate toCommand() {
            return new BidComparisonCommands.Generate(
                    rfqNo,
                    purchaseOrgId,
                    currency,
                    candidates.stream().map(CandidateRequest::toCommand).toList());
        }
    }

    public record CandidateRequest(
            Long candidateId,
            @Positive long supplierId,
            String supplierName,
            @NotBlank String quoteNo,
            @NotBlank String skuCode,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quoteQty,
            @NotNull @DecimalMin("0") BigDecimal unitPrice,
            @NotNull @DecimalMin("0") BigDecimal taxRate,
            @PositiveOrZero int deliveryDays,
            @DecimalMin("0") BigDecimal supplierScore,
            @DecimalMin("0") BigDecimal transportScore,
            @DecimalMin("0") BigDecimal estimatedFreightCost) {

        BidComparisonCommands.Candidate toCommand() {
            return new BidComparisonCommands.Candidate(
                    candidateId,
                    supplierId,
                    supplierName,
                    quoteNo,
                    skuCode,
                    quoteQty,
                    unitPrice,
                    taxRate,
                    deliveryDays,
                    supplierScore,
                    transportScore,
                    estimatedFreightCost);
        }
    }

    public record AwardRequest(
            @PositiveOrZero int version,
            @Positive long candidateId,
            @NotBlank String reason,
            boolean activatePurchasePrice,
            @Min(1) @Max(3) int priceType,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {

        BidComparisonCommands.Award toCommand() {
            return new BidComparisonCommands.Award(
                    version,
                    candidateId,
                    reason,
                    activatePurchasePrice,
                    priceType,
                    effectiveFrom,
                    effectiveTo);
        }
    }
}
