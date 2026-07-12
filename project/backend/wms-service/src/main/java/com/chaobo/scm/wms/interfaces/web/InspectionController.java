package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.inspection.InspectionApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wms/v1/inspections")
public class InspectionController {
    private final InspectionApplicationService service;

    public InspectionController(InspectionApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<InspectionApplicationService.Result> create(
            @Valid @RequestBody Create body,
            HttpServletRequest request
    ) {
        return ok(service.create(body.inspectionNo(), body.receiptId(), body.inspectQty(), op(request)), request);
    }

    @PostMapping("/{no}/result")
    public ApiResponse<InspectionApplicationService.Result> result(
            @PathVariable String no,
            @Valid @RequestBody Result body,
            HttpServletRequest request
    ) {
        return ok(service.result(no, body.version(), body.qualifiedQty(), body.unqualifiedQty(), op(request)), request);
    }

    private static long op(HttpServletRequest request) {
        var value = request.getHeader("X-Operator-Id");
        return value == null || value.isBlank() ? 0 : Long.parseLong(value);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record Create(
            @NotBlank String inspectionNo,
            @Positive long receiptId,
            @NotNull @DecimalMin("0") BigDecimal inspectQty
    ) {
    }

    public record Result(
            @PositiveOrZero int version,
            @NotNull @DecimalMin("0") BigDecimal qualifiedQty,
            @NotNull @DecimalMin("0") BigDecimal unqualifiedQty
    ) {
    }
}
