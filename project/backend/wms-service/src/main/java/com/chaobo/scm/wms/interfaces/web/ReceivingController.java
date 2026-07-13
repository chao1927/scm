package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.receiving.ReceivingApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wms/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:receiving:write')")
public class ReceivingController {
    private final ReceivingApplicationService service;

    public ReceivingController(ReceivingApplicationService service) {
        this.service = service;
    }

    @PostMapping("/receipts")
    public ApiResponse<ReceivingApplicationService.Result> open(
            @Valid @RequestBody OpenRequest body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        var command = new ReceivingApplicationService.Open(
                body.receiptNo(),
                body.inboundId(),
                body.skuCode(),
                body.expectedQty()
        );
        return ok(service.open(command, WmsAccessControl.operatorId(authentication)), request);
    }

    @PostMapping("/pda/receipts/scan")
    public ApiResponse<ReceivingApplicationService.Result> scan(
            @Valid @RequestBody ScanRequest body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        var command = new ReceivingApplicationService.Scan(
                body.receiptNo(),
                body.version(),
                body.receivedQty(),
                body.rejectedQty(),
                body.rejectReason(),
                request.getHeader("X-Idempotency-Key")
        );
        return ok(service.scan(command, WmsAccessControl.operatorId(authentication)), request);
    }

    @PostMapping("/receipts/{receiptNo}/submit")
    public ApiResponse<ReceivingApplicationService.Result> submit(
            @PathVariable String receiptNo,
            @Valid @RequestBody VersionRequest body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        return ok(service.submit(receiptNo, body.version(), WmsAccessControl.operatorId(authentication)), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record OpenRequest(
            @NotBlank String receiptNo,
            @Positive long inboundId,
            @NotBlank String skuCode,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal expectedQty
    ) {
    }

    public record ScanRequest(
            @NotBlank String receiptNo,
            @PositiveOrZero int version,
            @NotNull @DecimalMin("0") BigDecimal receivedQty,
            @NotNull @DecimalMin("0") BigDecimal rejectedQty,
            String rejectReason
    ) {
    }

    public record VersionRequest(@PositiveOrZero int version) {
    }
}
