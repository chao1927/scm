package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.inbox.WmsInboundEventApplicationService;
import com.chaobo.scm.wms.infrastructure.security.WmsAccessControl;
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
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'wms:*', 'wms:event:manage')")
public class WmsInboundEventController {
    private final WmsInboundEventApplicationService service;

    public WmsInboundEventController(WmsInboundEventApplicationService service) {
        this.service = service;
    }

    @PostMapping("/internal/wms/v1/events")
    public ApiResponse<WmsInboundEventApplicationService.ConsumeResult> consume(
            @Valid @RequestBody EventRequest body,
            HttpServletRequest request,
            Authentication authentication
    ) {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope(
                body.sourceSystem(),
                body.eventCode(),
                body.eventType(),
                body.payload()
        );
        return ok(service.consume(envelope, WmsAccessControl.operatorId(authentication)), request);
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
            HttpServletRequest request,
            Authentication authentication
    ) {
        return ok(service.replay(inboxId, WmsAccessControl.operatorId(authentication)), request);
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
