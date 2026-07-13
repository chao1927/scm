package com.chaobo.scm.integration.interfaces.web;

import com.chaobo.scm.integration.application.IntegrationApplicationService;
import com.chaobo.scm.integration.application.IntegrationDispatchRuntimeApplicationService;
import com.chaobo.scm.integration.application.IntegrationEndpointContractApplicationService;
import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationControllerTest {
    @Test
    void delegatesRouteAndMessageEndpoints() {
        StubIntegrationService service = new StubIntegrationService();
        StubDispatchRuntimeService dispatchRuntimeService = new StubDispatchRuntimeService();
        StubEndpointContractService endpointContractService = new StubEndpointContractService();
        IntegrationController controller = new IntegrationController(service, dispatchRuntimeService,
                endpointContractService);
        IntegrationApplicationService.CreateRouteCommand routeCommand =
                new IntegrationApplicationService.CreateRouteCommand(
                        "PurchaseOrderReleased", "PURCHASE", "WMS", "MQ", 1001L, "route-1");
        IntegrationApplicationService.AcceptMessageCommand messageCommand =
                new IntegrationApplicationService.AcceptMessageCommand(
                        "PurchaseOrderReleased", "PURCHASE", "PO-1", "event-1", "{}", 1001L);
        IntegrationApplicationService.CreateEndpointCommand endpointCommand =
                new IntegrationApplicationService.CreateEndpointCommand("WMS", "HTTP",
                        "http://127.0.0.1:18080/events", 1000, 3, 1001L, "endpoint-1");

        IntegrationMapper.RouteRow route = controller.createRoute(routeCommand);
        IntegrationMapper.EndpointRow endpoint = controller.createEndpoint(endpointCommand);
        IntegrationEndpointContractApplicationService.EndpointVerificationResult verification =
                controller.verifyEndpoint(endpoint.endpointNo());
        List<IntegrationMapper.MessageRow> messages = controller.acceptEvent(messageCommand);

        assertThat(route.routeNo()).isEqualTo("IR100001");
        assertThat(endpoint.endpointNo()).isEqualTo("IE500001");
        assertThat(verification.valid()).isTrue();
        assertThat(messages).hasSize(1);
        assertThat(service.lastRouteCommand).isEqualTo(routeCommand);
        assertThat(service.lastEndpointCommand).isEqualTo(endpointCommand);
        assertThat(service.lastAcceptCommand).isEqualTo(messageCommand);
    }

    @Test
    void delegatesDispatchRuntimeEndpoints() {
        StubIntegrationService service = new StubIntegrationService();
        StubDispatchRuntimeService dispatchRuntimeService = new StubDispatchRuntimeService();
        StubEndpointContractService endpointContractService = new StubEndpointContractService();
        IntegrationController controller = new IntegrationController(service, dispatchRuntimeService,
                endpointContractService);
        IntegrationDispatchRuntimeApplicationService.DispatchRunCommand command =
                new IntegrationDispatchRuntimeApplicationService.DispatchRunCommand(10, 1001L, "run-1");

        IntegrationDispatchRuntimeApplicationService.DispatchRunResult result =
                controller.dispatchDueMessages(command);
        List<IntegrationMapper.DeliveryAttemptRow> attempts = controller.deliveryAttempts("IM200001");
        IntegrationMapper.DispatchSummaryRow summary = controller.dispatchSummary();

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(attempts).hasSize(1);
        assertThat(summary.dispatchedCount()).isEqualTo(1);
        assertThat(dispatchRuntimeService.lastCommand).isEqualTo(command);
    }

    static class StubIntegrationService extends IntegrationApplicationService {
        IntegrationApplicationService.CreateRouteCommand lastRouteCommand;
        IntegrationApplicationService.CreateEndpointCommand lastEndpointCommand;
        IntegrationApplicationService.AcceptMessageCommand lastAcceptCommand;

        StubIntegrationService() {
            super(null);
        }

        @Override
        public IntegrationMapper.RouteRow createRoute(IntegrationApplicationService.CreateRouteCommand command) {
            lastRouteCommand = command;
            return new IntegrationMapper.RouteRow(null, "IR100001", command.messageType(), command.sourceSystem(),
                    command.targetSystem(), command.channelType(), 1, 1);
        }

        @Override
        public IntegrationMapper.EndpointRow createEndpoint(IntegrationApplicationService.CreateEndpointCommand command) {
            lastEndpointCommand = command;
            return new IntegrationMapper.EndpointRow(null, "IE500001", command.targetSystem(), command.channelType(),
                    command.endpointUrl(), command.timeoutMillis(), command.failureThreshold(), 0, 1, 1);
        }

        @Override
        public List<IntegrationMapper.MessageRow> acceptEvent(IntegrationApplicationService.AcceptMessageCommand command) {
            lastAcceptCommand = command;
            return List.of(new IntegrationMapper.MessageRow(null, "IM200001", command.messageType(),
                    command.sourceSystem(), "WMS", command.businessNo(), command.idempotencyKey(),
                    command.payload(), 1, 0, null, 1));
        }
    }

    static class StubDispatchRuntimeService extends IntegrationDispatchRuntimeApplicationService {
        IntegrationDispatchRuntimeApplicationService.DispatchRunCommand lastCommand;

        StubDispatchRuntimeService() {
            super(null, null, null);
        }

        @Override
        public DispatchRunResult dispatchDueMessages(DispatchRunCommand command) {
            lastCommand = command;
            return new DispatchRunResult(1, 1, 0, 0, listDeliveryAttempts("IM200001"));
        }

        @Override
        public List<IntegrationMapper.DeliveryAttemptRow> listDeliveryAttempts(String messageNo) {
            return List.of(new IntegrationMapper.DeliveryAttemptRow("DA1", messageNo,
                    "PurchaseOrderReleased", "PURCHASE", "WMS", "LOCAL_ACK", true, null, 3,
                    LocalDateTime.now()));
        }

        @Override
        public IntegrationMapper.DispatchSummaryRow dispatchSummary() {
            return new IntegrationMapper.DispatchSummaryRow(0, 1, 0, 0, 0);
        }
    }

    static class StubEndpointContractService extends IntegrationEndpointContractApplicationService {
        StubEndpointContractService() {
            super(null);
        }

        @Override
        public EndpointVerificationResult verifyEndpoint(String endpointNo) {
            return new EndpointVerificationResult(endpointNo, "WMS", "HTTP", true,
                    "endpoint contract is valid", java.util.Map.of());
        }
    }
}
