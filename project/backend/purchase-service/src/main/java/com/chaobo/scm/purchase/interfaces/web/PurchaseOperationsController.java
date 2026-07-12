package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.purchase.application.integration.InboundEventReplayApplicationService;
import com.chaobo.scm.purchase.application.operations.PurchaseOperationsApplicationService;
import com.chaobo.scm.purchase.application.operations.PurchaseOperationsViews;
import com.chaobo.scm.purchase.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchase/v1/operations")
public class PurchaseOperationsController {
    private final PurchaseOperationsApplicationService operations;
    private final InboundEventReplayApplicationService replayService;
    private final CommandContextFactory contexts;

    public PurchaseOperationsController(PurchaseOperationsApplicationService operations,
                                        InboundEventReplayApplicationService replayService,
                                        CommandContextFactory contexts) {
        this.operations = operations;
        this.replayService = replayService;
        this.contexts = contexts;
    }

    @GetMapping("/failed-events")
    public ApiResponse<List<PurchaseOperationsViews.FailedEvent>> failedEvents(HttpServletRequest request) {
        return ok(operations.failedEvents(), request);
    }

    @PostMapping("/failed-events/{id}/replay")
    public ApiResponse<Void> replay(@PathVariable long id, @Valid @RequestBody ReplayRequest body,
                                    HttpServletRequest request, Authentication authentication) {
        replayService.replay(id, body.reason(), contexts.create(request, authentication));
        return ok(null, request);
    }

    private <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record ReplayRequest(@NotBlank String reason) {
    }
}
