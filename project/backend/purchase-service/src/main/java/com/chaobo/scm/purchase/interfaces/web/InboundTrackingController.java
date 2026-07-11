package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.inbound.InboundCommands;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingApplicationService;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingQueryApplicationService;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingView;
import com.chaobo.scm.purchase.application.shared.CommandResult;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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

@RestController
@RequestMapping("/api/purchase/v1/inbounds")
public class InboundTrackingController {
    private final InboundTrackingApplicationService applicationService;
    private final InboundTrackingQueryApplicationService queryService;
    private final CommandContextFactory contexts;

    public InboundTrackingController(InboundTrackingApplicationService applicationService,
                                     InboundTrackingQueryApplicationService queryService,
                                     CommandContextFactory contexts) {
        this.applicationService = applicationService;
        this.queryService = queryService;
        this.contexts = contexts;
    }

    @GetMapping
    public ApiResponse<PageResult<InboundTrackingView>> page(@RequestParam(required = false) Long purchaseOrgId,
                                                             @RequestParam(required = false) String orderNo,
                                                             @RequestParam(required = false) String asnNo,
                                                             @RequestParam(required = false) String warehouseCode,
                                                             @RequestParam(required = false) Integer status,
                                                             @RequestParam(defaultValue = "1") int pageNo,
                                                             @RequestParam(defaultValue = "20") int pageSize,
                                                             HttpServletRequest request) {
        return ok(queryService.page(purchaseOrgId, scope(request), orderNo, asnNo, warehouseCode, status,
                pageNo, pageSize), request);
    }

    @GetMapping("/{inboundNo}")
    public ApiResponse<InboundTrackingView> detail(@PathVariable String inboundNo, HttpServletRequest request) {
        return ok(queryService.detail(inboundNo, scope(request)), request);
    }

    @PostMapping("/asns")
    public ApiResponse<CommandResult> recordAsn(@Valid @RequestBody RecordAsnRequest body,
                                                HttpServletRequest request,
                                                Authentication authentication) {
        return ok(applicationService.recordAsn(body.toCommand(), contexts.create(request, authentication)), request);
    }

    @PostMapping("/{inboundNo}/sync-wms")
    public ApiResponse<CommandResult> syncWms(@PathVariable String inboundNo,
                                              @Valid @RequestBody SyncWmsRequest body,
                                              HttpServletRequest request,
                                              Authentication authentication) {
        return ok(applicationService.syncWms(inboundNo, body.toCommand(), contexts.create(request, authentication)),
                request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    private Long scope(HttpServletRequest request) {
        var value = request.getHeader("X-Purchase-Org-Id");
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    public record RecordAsnRequest(@NotBlank String orderNo,
                                   @NotBlank String asnNo,
                                   @Positive long supplierId,
                                   @Positive long purchaseOrgId,
                                   String warehouseCode,
                                   @NotBlank String skuCode,
                                   @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal notifiedQty) {
        InboundCommands.RecordAsn toCommand() {
            return new InboundCommands.RecordAsn(orderNo, asnNo, supplierId, purchaseOrgId, warehouseCode,
                    skuCode, notifiedQty);
        }
    }

    public record SyncWmsRequest(@PositiveOrZero int version,
                                 @NotNull @DecimalMin("0") BigDecimal receivedQty,
                                 @NotNull @DecimalMin("0") BigDecimal qualifiedQty,
                                 @NotNull @DecimalMin("0") BigDecimal unqualifiedQty,
                                 @NotNull @DecimalMin("0") BigDecimal putawayQty,
                                 String reason) {
        InboundCommands.SyncWms toCommand() {
            return new InboundCommands.SyncWms(version, receivedQty, qualifiedQty, unqualifiedQty, putawayQty,
                    reason);
        }
    }
}
