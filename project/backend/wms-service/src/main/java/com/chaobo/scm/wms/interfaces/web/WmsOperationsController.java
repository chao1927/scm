package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.outbox.WmsOutboxDispatchApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wms/v1/operations")
public class WmsOperationsController {
    private final WmsOutboxDispatchApplicationService outbox;

    public WmsOperationsController(WmsOutboxDispatchApplicationService outbox) {
        this.outbox = outbox;
    }

    @PostMapping("/outbox/dispatch")
    public ApiResponse<WmsOutboxDispatchApplicationService.DispatchResult> dispatch(
            @Valid @RequestBody DispatchRequest body,
            HttpServletRequest request
    ) {
        return ok(outbox.dispatchPending(body.limit()), request);
    }

    @GetMapping("/outbox/failed-events")
    public ApiResponse<List<WmsOutboxDispatchApplicationService.EventView>> failed(
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest request
    ) {
        return ok(outbox.failedEvents(limit), request);
    }

    @PostMapping("/outbox/failed-events/{eventId}/retry")
    public ApiResponse<Void> retry(@PathVariable long eventId, HttpServletRequest request) {
        outbox.retry(eventId);
        return ok(null, request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record DispatchRequest(@Positive int limit) {
    }
}
