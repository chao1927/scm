package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.inspection.InspectionApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
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
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wms/v1/inspections")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:inspection:write')")
public class InspectionController {
    private final InspectionApplicationService service;

    public InspectionController(InspectionApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<InspectionApplicationService.Result> create(
            @Valid @RequestBody Create body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        return ok(service.create(body.inspectionNo(), body.receiptId(), body.inspectQty(),
                WmsAccessControl.operatorId(authentication)), request);
    }

    @PostMapping("/{no}/result")
    public ApiResponse<InspectionApplicationService.Result> result(
            @PathVariable String no,
            @Valid @RequestBody Result body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        return ok(service.result(no, body.version(), body.qualifiedQty(), body.unqualifiedQty(),
                WmsAccessControl.operatorId(authentication)), request);
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
