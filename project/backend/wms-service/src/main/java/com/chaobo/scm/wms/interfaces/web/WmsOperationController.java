package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.operation.WmsOperationApplicationService;
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
public class WmsOperationController {
    private final WmsOperationApplicationService service;

    public WmsOperationController(WmsOperationApplicationService service) {
        this.service = service;
    }

    @PostMapping("/handovers")
    public ApiResponse<WmsOperationApplicationService.StatusResult> createHandover(@Valid @RequestBody CreateHandover body, HttpServletRequest request) {
        return ok(service.createHandover(body.handoverNo(), body.outboundId()), request);
    }

    @PostMapping("/handovers/confirm")
    public ApiResponse<WmsOperationApplicationService.StatusResult> confirmHandover(@Valid @RequestBody Confirm body, HttpServletRequest request) {
        return ok(service.confirmHandover(body.no(), body.version()), request);
    }

    @PostMapping("/stocktakes")
    public ApiResponse<WmsOperationApplicationService.StatusResult> createStocktake(@Valid @RequestBody CreateStocktake body, HttpServletRequest request) {
        return ok(service.createStocktake(body.stocktakeNo(), body.warehouseId(), body.sku(), body.differenceQty()), request);
    }

    @PostMapping("/stocktakes/confirm-difference")
    public ApiResponse<WmsOperationApplicationService.StatusResult> confirmStocktake(@Valid @RequestBody Confirm body, HttpServletRequest request) {
        return ok(service.confirmStocktake(body.no(), body.version()), request);
    }

    @PostMapping("/warehouse-exceptions")
    public ApiResponse<WmsOperationApplicationService.StatusResult> createException(@Valid @RequestBody CreateException body, HttpServletRequest request) {
        return ok(service.createException(body.exceptionNo(), body.reason()), request);
    }

    @PostMapping("/warehouse-exceptions/close")
    public ApiResponse<WmsOperationApplicationService.StatusResult> closeException(@Valid @RequestBody Confirm body, HttpServletRequest request) {
        return ok(service.closeException(body.no(), body.version()), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record CreateHandover(@NotBlank String handoverNo, @Positive long outboundId) {}
    public record CreateStocktake(@NotBlank String stocktakeNo, @Positive long warehouseId, @NotBlank String sku, @NotNull @DecimalMin("0") BigDecimal differenceQty) {}
    public record CreateException(@NotBlank String exceptionNo, @NotBlank String reason) {}
    public record Confirm(@NotBlank String no, @PositiveOrZero int version) {}
}
