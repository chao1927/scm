package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.inbound.WmsCommandResult;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;

@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:inbound:write')")
public class InboundOrderController {
    private final InboundOrderApplicationService service;

    public InboundOrderController(InboundOrderApplicationService service) {
        this.service = service;
    }

    @PostMapping("/openapi/wms/v1/inbound-orders")
    public ApiResponse<WmsCommandResult> create(@Valid @RequestBody CreateRequest body, HttpServletRequest request,
                                                Authentication authentication) {
        var source = request.getHeader("X-Source-System");
        var idempotencyKey = request.getHeader("X-Idempotency-Key");
        if (source == null || source.isBlank() || !source.equals(body.sourceType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "来源系统与入库来源不一致");
        }
        WmsAccessControl.requireWarehouse(authentication, body.warehouseId());
        return ok(service.create(new InboundOrderApplicationService.Create(body.sourceType(), body.sourceNo(),
                body.warehouseId(), body.expectedArrivalAt(), idempotencyKey),
                WmsAccessControl.operatorId(authentication)), request);
    }

    @PostMapping("/api/wms/v1/inbound-orders/{id}/cancel")
    public ApiResponse<WmsCommandResult> cancel(@PathVariable long id, @Valid @RequestBody CancelRequest body,
                                                HttpServletRequest request, Authentication authentication) {
        long warehouseId = warehouseScope(request);
        WmsAccessControl.requireWarehouse(authentication, warehouseId);
        return ok(service.cancel(id, new InboundOrderApplicationService.Cancel(body.version(), body.reason()),
                warehouseId, WmsAccessControl.operatorId(authentication)), request);
    }

    private static long warehouseScope(HttpServletRequest request) {
        var value = request.getHeader("X-Warehouse-Id");
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "缺少仓库范围");
        }
        return Long.parseLong(value);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record CreateRequest(@NotBlank String sourceType, @NotBlank String sourceNo, @Positive long warehouseId,
                                OffsetDateTime expectedArrivalAt) {
    }

    public record CancelRequest(@PositiveOrZero int version, @NotBlank String reason) {
    }
}
