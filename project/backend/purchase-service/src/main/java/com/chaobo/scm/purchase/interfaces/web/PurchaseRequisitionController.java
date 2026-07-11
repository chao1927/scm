package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.requisition.PurchaseRequisitionApplicationService;
import com.chaobo.scm.purchase.application.requisition.PurchaseRequisitionCommands;
import com.chaobo.scm.purchase.application.requisition.PurchaseRequisitionQueryApplicationService;
import com.chaobo.scm.purchase.application.requisition.PurchaseRequisitionView;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase/v1/requisitions")
public class PurchaseRequisitionController {
    private final PurchaseRequisitionApplicationService applicationService;
    private final PurchaseRequisitionQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public PurchaseRequisitionController(
            PurchaseRequisitionApplicationService applicationService,
            PurchaseRequisitionQueryApplicationService queryService,
            CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<PurchaseRequisitionView>> page(
            @RequestParam(required = false) Long purchaseOrgId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        var scope = optionalLong(request.getHeader("X-Purchase-Org-Id"));
        return ok(queryService.page(purchaseOrgId, scope, status, keyword, pageNo, pageSize), request);
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseRequisitionView> detail(@PathVariable long id, HttpServletRequest request) {
        var scope = optionalLong(request.getHeader("X-Purchase-Org-Id"));
        return ok(queryService.detail(id, scope), request);
    }

    @PostMapping
    public ApiResponse<CommandResult> create(
            @Valid @RequestBody SaveRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        var command = body.toCommand(null);
        return ok(applicationService.create(command, contexts.create(request, authentication)), request);
    }

    @PutMapping("/{id}")
    public ApiResponse<CommandResult> update(
            @PathVariable long id,
            @Valid @RequestBody SaveRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        var command = body.toCommand(id);
        return ok(applicationService.update(id, command, contexts.create(request, authentication)), request);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<CommandResult> submit(
            @PathVariable long id,
            @Valid @RequestBody VersionRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        return ok(applicationService.submit(id, body.version(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<CommandResult> approve(
            @PathVariable long id,
            @Valid @RequestBody ApproveRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        var command = new PurchaseRequisitionCommands.Approve(body.version(), body.approvedQuantities());
        return ok(applicationService.approve(id, command, contexts.create(request, authentication)), request);
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<CommandResult> reject(
            @PathVariable long id,
            @Valid @RequestBody RejectRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        var command = new PurchaseRequisitionCommands.Reject(body.version(), body.reason());
        return ok(applicationService.reject(id, command, contexts.create(request, authentication)), request);
    }

    @PostMapping("/{id}/convert")
    public ApiResponse<CommandResult> convert(
            @PathVariable long id,
            @Valid @RequestBody ConvertRequest body,
            HttpServletRequest request,
            Authentication authentication) {
        var command = new PurchaseRequisitionCommands.Convert(
                body.version(),
                body.targetType(),
                body.targetNo(),
                body.quantities());
        return ok(applicationService.convert(id, command, contexts.create(request, authentication)), request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private static Long optionalLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    public record SaveRequest(
            @Positive long applicantId,
            @Positive long purchaseOrgId,
            @Positive long demandDepartmentId,
            String reason,
            @PositiveOrZero int version,
            @NotEmpty List<@Valid LineRequest> lines) {

        PurchaseRequisitionCommands.Save toCommand(Long id) {
            return new PurchaseRequisitionCommands.Save(
                    id,
                    version,
                    applicantId,
                    purchaseOrgId,
                    demandDepartmentId,
                    reason,
                    lines.stream().map(LineRequest::toCommand).toList());
        }
    }

    public record LineRequest(
            Long lineId,
            @NotBlank String skuCode,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal requestedQty,
            String purchaseUnit,
            @NotNull @FutureOrPresent LocalDate requiredDate,
            String remark) {

        PurchaseRequisitionCommands.Line toCommand() {
            return new PurchaseRequisitionCommands.Line(
                    lineId,
                    skuCode,
                    requestedQty,
                    purchaseUnit,
                    requiredDate,
                    remark);
        }
    }

    public record VersionRequest(@PositiveOrZero int version) {
    }

    public record ApproveRequest(
            @PositiveOrZero int version,
            @NotEmpty Map<@Positive Long, @DecimalMin("0") BigDecimal> approvedQuantities) {
    }

    public record RejectRequest(
            @PositiveOrZero int version,
            @NotBlank String reason) {
    }

    public record ConvertRequest(
            @PositiveOrZero int version,
            @NotBlank String targetType,
            @NotBlank String targetNo,
            @NotEmpty Map<@Positive Long, @DecimalMin(value = "0", inclusive = false) BigDecimal> quantities) {
    }
}
