package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.inventory.application.InventoryEventApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class InventoryEventController {
    private final InventoryEventApplicationService service;

    public InventoryEventController(InventoryEventApplicationService service) {
        this.service = service;
    }

    @PostMapping("/internal/inventory/v1/events")
    public ApiResponse<InventoryEventApplicationService.ConsumeResult> consume(@Valid @RequestBody EventRequest body, HttpServletRequest request) {
        return ok(service.consumeWmsEvent(new InventoryEventApplicationService.EventEnvelope(body.sourceSystem(), body.eventCode(), body.eventType(), body.payload())), request);
    }

    @PostMapping("/api/inventory/v1/operations/outbox/dispatch")
    public ApiResponse<InventoryEventApplicationService.DispatchResult> dispatch(@Valid @RequestBody DispatchRequest body, HttpServletRequest request) {
        return ok(service.dispatch(body.limit()), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record EventRequest(@NotBlank String sourceSystem, @NotBlank String eventCode, @NotBlank String eventType, @NotBlank String payload) {}
    public record DispatchRequest(@Positive int limit) {}
}
