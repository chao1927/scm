package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.inbox.WmsInboundEventApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class WmsInboundEventController {
    private final WmsInboundEventApplicationService service;

    public WmsInboundEventController(WmsInboundEventApplicationService service) {
        this.service = service;
    }

    @PostMapping("/internal/wms/v1/events")
    public ApiResponse<WmsInboundEventApplicationService.ConsumeResult> consume(
            @Valid @RequestBody EventRequest body,
            HttpServletRequest request
    ) {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope(
                body.sourceSystem(),
                body.eventCode(),
                body.eventType(),
                body.payload()
        );
        return ok(service.consume(envelope, operator(request)), request);
    }

    @GetMapping("/api/wms/v1/operations/inbox/failed-events")
    public ApiResponse<List<WmsInboundEventApplicationService.FailedEventView>> failed(
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest request
    ) {
        return ok(service.failedEvents(limit), request);
    }

    @PostMapping("/api/wms/v1/operations/inbox/failed-events/{inboxId}/replay")
    public ApiResponse<WmsInboundEventApplicationService.ConsumeResult> replay(
            @PathVariable long inboxId,
            HttpServletRequest request
    ) {
        return ok(service.replay(inboxId, operator(request)), request);
    }

    private static long operator(HttpServletRequest request) {
        var value = request.getHeader("X-Operator-Id");
        return value == null || value.isBlank() ? 0 : Long.parseLong(value);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record EventRequest(
            @NotBlank String sourceSystem,
            @NotBlank String eventCode,
            @NotBlank String eventType,
            @NotBlank String payload
    ) {
    }
}
