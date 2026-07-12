package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.wave.WavePickingApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wms/v1")
public class WavePickingController {
    private final WavePickingApplicationService service;

    public WavePickingController(WavePickingApplicationService service) {
        this.service = service;
    }

    @PostMapping("/waves")
    public ApiResponse<WavePickingApplicationService.WaveResult> createWave(
            @Valid @RequestBody CreateWave body,
            HttpServletRequest request
    ) {
        return ok(service.createWave(body.waveNo(), body.warehouseId()), request);
    }

    @PostMapping("/waves/release")
    public ApiResponse<WavePickingApplicationService.WaveResult> releaseWave(
            @Valid @RequestBody ReleaseWave body,
            HttpServletRequest request
    ) {
        return ok(service.releaseWave(body.waveNo(), body.version()), request);
    }

    @PostMapping("/pick-tasks")
    public ApiResponse<WavePickingApplicationService.PickResult> createPickTask(
            @Valid @RequestBody CreatePickTask body,
            HttpServletRequest request
    ) {
        return ok(service.createPickTask(body.taskNo(), body.waveId(), body.outboundId(), body.sku(), body.requiredQty()), request);
    }

    @PostMapping("/pda/pick-tasks/scan")
    public ApiResponse<WavePickingApplicationService.PickResult> scanPick(
            @Valid @RequestBody ScanPick body,
            HttpServletRequest request
    ) {
        return ok(service.scanPick(body.taskNo(), body.version(), body.qty()), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record CreateWave(@NotBlank String waveNo, @Positive long warehouseId) {
    }

    public record ReleaseWave(@NotBlank String waveNo, @PositiveOrZero int version) {
    }

    public record CreatePickTask(
            @NotBlank String taskNo,
            @Positive long waveId,
            @Positive long outboundId,
            @NotBlank String sku,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal requiredQty
    ) {
    }

    public record ScanPick(
            @NotBlank String taskNo,
            @PositiveOrZero int version,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal qty
    ) {
    }
}
