package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.receiving.ReceivingApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wms/v1")
public class ReceivingController {
    private final ReceivingApplicationService service;

    public ReceivingController(ReceivingApplicationService service) {
        this.service = service;
    }

    @PostMapping("/receipts")
    public ApiResponse<ReceivingApplicationService.Result> open(
            @Valid @RequestBody OpenRequest body,
            HttpServletRequest request
    ) {
        var command = new ReceivingApplicationService.Open(
                body.receiptNo(),
                body.inboundId(),
                body.skuCode(),
                body.expectedQty()
        );
        return ok(service.open(command, operator(request)), request);
    }

    @PostMapping("/pda/receipts/scan")
    public ApiResponse<ReceivingApplicationService.Result> scan(
            @Valid @RequestBody ScanRequest body,
            HttpServletRequest request
    ) {
        var command = new ReceivingApplicationService.Scan(
                body.receiptNo(),
                body.version(),
                body.receivedQty(),
                body.rejectedQty(),
                body.rejectReason(),
                request.getHeader("X-Idempotency-Key")
        );
        return ok(service.scan(command, operator(request)), request);
    }

    @PostMapping("/receipts/{receiptNo}/submit")
    public ApiResponse<ReceivingApplicationService.Result> submit(
            @PathVariable String receiptNo,
            @Valid @RequestBody VersionRequest body,
            HttpServletRequest request
    ) {
        return ok(service.submit(receiptNo, body.version(), operator(request)), request);
    }

    private static long operator(HttpServletRequest request) {
        var value = request.getHeader("X-Operator-Id");
        return value == null || value.isBlank() ? 0 : Long.parseLong(value);
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
