package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.rfq.RfqApplicationService;
import com.chaobo.scm.purchase.application.rfq.RfqCommands;
import com.chaobo.scm.purchase.application.rfq.RfqQueryApplicationService;
import com.chaobo.scm.purchase.application.rfq.RfqView;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/purchase/v1/rfqs")
public class RfqController {
    private final RfqApplicationService applicationService;
    private final RfqQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public RfqController(
            RfqApplicationService applicationService,
            RfqQueryApplicationService queryService,
            CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<RfqView>> page(
            @RequestParam(required = false) Long purchaseOrgId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime deadlineTo,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        var scope = optionalLong(request.getHeader("X-Purchase-Org-Id"));
        return ok(queryService.page(
                purchaseOrgId,
                scope,
                status,
                categoryCode,
                supplierId,
                deadlineFrom,
                deadlineTo,
                pageNo,
                pageSize), request);
    }

    @GetMapping("/{rfqNo}")
    public ApiResponse<RfqView> detail(@PathVariable String rfqNo, HttpServletRequest request) {
        var scope = optionalLong(request.getHeader("X-Purchase-Org-Id"));
        return ok(queryService.detailByNo(rfqNo, scope), request);
    }

    @PostMapping
    public ApiResponse<CommandResult> create(
            @Valid @RequestBody CreateRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(applicationService.create(body.toCommand(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{rfqNo}/publish")
    public ApiResponse<CommandResult> publish(
            @PathVariable String rfqNo,
            @Valid @RequestBody VersionRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(
                applicationService.publish(rfqNo, new RfqCommands.Version(body.version()),
                        contexts.create(request, authentication)),
                request);
    }

    @PostMapping("/{rfqNo}/close")
    public ApiResponse<CommandResult> close(
            @PathVariable String rfqNo,
            @Valid @RequestBody CloseRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(
                applicationService.close(rfqNo, new RfqCommands.Close(body.version(), body.reason()),
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
            @Min(1) @Max(3) int rfqType,
            @Positive long purchaseOrgId,
            String categoryCode,
            String sourceRequisitionNo,
            @NotNull @Future OffsetDateTime quoteDeadline,
            @NotEmpty List<@Valid LineRequest> lines,
            @NotEmpty List<@Positive Long> invitedSupplierIds) {

        RfqCommands.Create toCommand() {
            return new RfqCommands.Create(
                    rfqType,
                    purchaseOrgId,
                    categoryCode,
                    sourceRequisitionNo,
                    quoteDeadline,
                    lines.stream().map(LineRequest::toCommand).toList(),
                    invitedSupplierIds);
        }
    }

    public record LineRequest(
            Long lineId,
            @NotBlank String skuCode,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal targetQty,
            @NotBlank String uom,
            @FutureOrPresent LocalDate requiredDeliveryDate,
            String qualityRequirement) {

        RfqCommands.Line toCommand() {
            return new RfqCommands.Line(
                    lineId,
                    skuCode,
                    targetQty,
                    uom,
                    requiredDeliveryDate,
                    qualityRequirement);
        }
    }

    public record VersionRequest(@PositiveOrZero int version) {
    }

    public record CloseRequest(
            @PositiveOrZero int version,
            @NotBlank String reason) {
    }
}
