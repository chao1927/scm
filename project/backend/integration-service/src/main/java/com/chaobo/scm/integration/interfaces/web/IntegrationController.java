package com.chaobo.scm.integration.interfaces.web;

import com.chaobo.scm.integration.application.IntegrationApplicationService;
import com.chaobo.scm.integration.application.IntegrationDispatchRuntimeApplicationService;
import com.chaobo.scm.integration.application.IntegrationEndpointContractApplicationService;
import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
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
public class IntegrationController {
    private final IntegrationApplicationService service;
    private final IntegrationDispatchRuntimeApplicationService dispatchRuntimeService;
    private final IntegrationEndpointContractApplicationService endpointContractService;

    public IntegrationController(IntegrationApplicationService service,
                                 IntegrationDispatchRuntimeApplicationService dispatchRuntimeService,
                                 IntegrationEndpointContractApplicationService endpointContractService) {
        this.service = service;
        this.dispatchRuntimeService = dispatchRuntimeService;
        this.endpointContractService = endpointContractService;
    }

    @PostMapping("/api/integration/v1/routes")
    public IntegrationMapper.RouteRow createRoute(@RequestBody IntegrationApplicationService.CreateRouteCommand command) {
        return service.createRoute(command);
    }

    @PostMapping("/api/integration/v1/routes/{routeNo}/disable")
    public IntegrationMapper.RouteRow disableRoute(@PathVariable String routeNo,
                                                   @RequestBody IntegrationApplicationService.DisableRouteCommand command) {
        return service.disableRoute(routeNo, command);
    }

    @GetMapping("/api/integration/v1/routes")
    public List<IntegrationMapper.RouteRow> routes() {
        return service.listRoutes();
    }

    @PostMapping("/api/integration/v1/endpoints")
    public IntegrationMapper.EndpointRow createEndpoint(
            @RequestBody IntegrationApplicationService.CreateEndpointCommand command) {
        return service.createEndpoint(command);
    }

    @PostMapping("/api/integration/v1/endpoints/{endpointNo}/disable")
    public IntegrationMapper.EndpointRow disableEndpoint(
            @PathVariable String endpointNo,
            @RequestBody IntegrationApplicationService.DisableEndpointCommand command) {
        return service.disableEndpoint(endpointNo, command);
    }

    @GetMapping("/api/integration/v1/endpoints")
    public List<IntegrationMapper.EndpointRow> endpoints() {
        return service.listEndpoints();
    }

    @PostMapping("/api/integration/v1/endpoints/{endpointNo}/verify")
    public IntegrationEndpointContractApplicationService.EndpointVerificationResult verifyEndpoint(
            @PathVariable String endpointNo) {
        return endpointContractService.verifyEndpoint(endpointNo);
    }

    @PostMapping("/openapi/integration/v1/events")
    public List<IntegrationMapper.MessageRow> acceptEvent(@RequestBody IntegrationApplicationService.AcceptMessageCommand command) {
        return service.acceptEvent(command);
    }

    @PostMapping("/openapi/integration/v1/commands")
    public List<IntegrationMapper.MessageRow> acceptCommand(@RequestBody IntegrationApplicationService.AcceptMessageCommand command) {
        return service.acceptEvent(command);
    }

    @PostMapping("/api/integration/v1/messages/{messageNo}/dispatch")
    public IntegrationMapper.MessageRow dispatch(@PathVariable String messageNo,
                                                 @RequestBody IntegrationApplicationService.DispatchCommand command) {
        return service.dispatch(messageNo, command);
    }

    @PostMapping("/api/integration/v1/messages/{messageNo}/retry")
    public IntegrationMapper.MessageRow retry(@PathVariable String messageNo,
                                              @RequestBody IntegrationApplicationService.RetryCommand command) {
        return service.retry(messageNo, command);
    }

    @GetMapping("/api/integration/v1/messages")
    public List<IntegrationMapper.MessageRow> messages(@RequestParam(required = false) Integer status) {
        return service.listMessages(status);
    }

    @PostMapping("/api/integration/v1/dead-letters/{deadLetterNo}/replay")
    public IntegrationMapper.MessageRow replay(@PathVariable String deadLetterNo,
                                               @RequestBody IntegrationApplicationService.ReplayCommand command) {
        return service.replayDeadLetter(deadLetterNo, command);
    }

    @GetMapping("/api/integration/v1/dead-letters")
    public List<IntegrationMapper.DeadLetterRow> deadLetters() {
        return service.listDeadLetters();
    }

    @PostMapping("/api/integration/v1/dispatch-runs")
    public IntegrationDispatchRuntimeApplicationService.DispatchRunResult dispatchDueMessages(
            @RequestBody IntegrationDispatchRuntimeApplicationService.DispatchRunCommand command) {
        return dispatchRuntimeService.dispatchDueMessages(command);
    }

    @GetMapping("/api/integration/v1/delivery-attempts")
    public List<IntegrationMapper.DeliveryAttemptRow> deliveryAttempts(
            @RequestParam(required = false) String messageNo) {
        return dispatchRuntimeService.listDeliveryAttempts(messageNo);
    }

    @GetMapping("/api/integration/v1/operations/summary")
    public IntegrationMapper.DispatchSummaryRow dispatchSummary() {
        return dispatchRuntimeService.dispatchSummary();
    }
}
