package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:outbound:write')")
public class OutboundOrderController {
    private final OutboundApplicationService service;

    public OutboundOrderController(OutboundApplicationService service) {
        this.service = service;
    }

    @PostMapping("/openapi/wms/v1/outbound-orders")
    public ApiResponse<OutboundApplicationService.Result> create(
            @Valid @RequestBody Create body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        var source = request.getHeader("X-Source-System");
        if (source == null || !source.equals(body.sourceType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "来源系统与出库来源不一致");
        }
        if (request.getHeader("X-Idempotency-Key") == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少幂等键");
        }
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        return ok(service.create(body.sourceType(), body.sourceNo(), body.warehouseId(),
                WmsAccessControl.operatorId(authentication)), request);
    }

    @PostMapping("/api/wms/v1/outbound-orders/allocate")
    public ApiResponse<OutboundApplicationService.Result> allocate(
            @Valid @RequestBody Change body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        return ok(
                service.allocate(body.sourceType(), body.sourceNo(), body.warehouseId(), body.version(),
                        WmsAccessControl.operatorId(authentication)),
                request
        );
    }

    @PostMapping("/openapi/wms/v1/outbound-orders/cancel")
    public ApiResponse<OutboundApplicationService.Result> cancel(
            @Valid @RequestBody Cancel body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        return ok(
                service.cancel(
                        body.sourceType(),
                        body.sourceNo(),
                        body.warehouseId(),
                        body.version(),
                        body.reason(),
                        WmsAccessControl.operatorId(authentication)
                ),
                request
        );
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record Create(@NotBlank String sourceType, @NotBlank String sourceNo, @Positive long warehouseId) {
    }

    public record Change(
            @NotBlank String sourceType,
            @NotBlank String sourceNo,
            @Positive long warehouseId,
            @PositiveOrZero int version
    ) {
    }

    public record Cancel(
            @NotBlank String sourceType,
            @NotBlank String sourceNo,
            @Positive long warehouseId,
            @PositiveOrZero int version,
            @NotBlank String reason
    ) {
    }
}
