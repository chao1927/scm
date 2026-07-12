package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.putaway.PutawayApplicationService;
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
public class PutawayController {
    private final PutawayApplicationService service;

    public PutawayController(PutawayApplicationService service) {
        this.service = service;
    }

    @PostMapping("/putaway-tasks")
    public ApiResponse<PutawayApplicationService.Result> create(
            @Valid @RequestBody Create body,
            HttpServletRequest request
    ) {
        return ok(service.create(body.taskNo(), body.inspectionId(), body.requiredQty(), op(request)), request);
    }

    @PostMapping("/pda/putaway/scan")
    public ApiResponse<PutawayApplicationService.Result> scan(
            @Valid @RequestBody Scan body,
            HttpServletRequest request
    ) {
        return ok(
                service.scan(
                        body.taskNo(),
                        body.version(),
                        body.warehouseId(),
                        body.locationCode(),
                        body.skuCode(),
                        body.batchNo(),
                        body.qty(),
                        op(request)
                ),
                request
        );
    }

    private static long op(HttpServletRequest request) {
        var value = request.getHeader("X-Operator-Id");
        return value == null || value.isBlank() ? 0 : Long.parseLong(value);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record Create(
            @NotBlank String taskNo,
            @Positive long inspectionId,
            @NotNull @DecimalMin("0") BigDecimal requiredQty
    ) {
    }

    public record Scan(
            @NotBlank String taskNo,
            @PositiveOrZero int version,
            @Positive long warehouseId,
            @NotBlank String locationCode,
            @NotBlank String skuCode,
            String batchNo,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal qty
    ) {
    }
}
