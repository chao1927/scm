package com.chaobo.scm.integration.application;

import com.chaobo.scm.integration.domain.IntegrationMessageAggregate;
import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationApplicationServiceTest {
    @Test
    void acceptsEventByRouteAndReturnsIdempotentMessage() {
        MemoryIntegrationMapper mapper = new MemoryIntegrationMapper();
        IntegrationApplicationService service = new IntegrationApplicationService(mapper);
        service.createRoute(new IntegrationApplicationService.CreateRouteCommand(
                "PurchaseOrderReleased", "PURCHASE", "WMS", "MQ", 1001L, "route-1"));
        IntegrationApplicationService.AcceptMessageCommand command =
                new IntegrationApplicationService.AcceptMessageCommand(
                        "PurchaseOrderReleased", "PURCHASE", "PO-1", "event-1", "{\"po\":\"PO-1\"}", 1001L);

        List<IntegrationMapper.MessageRow> first = service.acceptEvent(command);
        List<IntegrationMapper.MessageRow> duplicate = service.acceptEvent(command);

        assertThat(first).hasSize(1);
        assertThat(first.getFirst().targetSystem()).isEqualTo("WMS");
        assertThat(duplicate.getFirst().messageNo()).isEqualTo(first.getFirst().messageNo());
    }

    @Test
    void missingRouteCreatesDeadLetter() {
        MemoryIntegrationMapper mapper = new MemoryIntegrationMapper();
        IntegrationApplicationService service = new IntegrationApplicationService(mapper);

        List<IntegrationMapper.MessageRow> rows = service.acceptEvent(
                new IntegrationApplicationService.AcceptMessageCommand(
                        "UnknownEvent", "OMS", "SO-1", "event-1", "{}", 1001L));

        assertThat(rows.getFirst().status()).isEqualTo(IntegrationMessageAggregate.DEAD_LETTER);
        assertThat(service.listDeadLetters()).hasSize(1);
    }

    @Test
    void failedMessageCanRetryAndDeadLetterCanReplay() {
        MemoryIntegrationMapper mapper = new MemoryIntegrationMapper();
        IntegrationApplicationService service = new IntegrationApplicationService(mapper);
        service.createRoute(new IntegrationApplicationService.CreateRouteCommand(
                "WaybillSigned", "TMS", "BMS", "MQ", 1001L, "route-1"));
        IntegrationMapper.MessageRow message = service.acceptEvent(
                new IntegrationApplicationService.AcceptMessageCommand(
                        "WaybillSigned", "TMS", "WB-1", "event-1", "{}", 1001L)).getFirst();

        message = service.dispatch(message.messageNo(),
                new IntegrationApplicationService.DispatchCommand(false, "MQ timeout", message.version(), 1001L, "d1"));
        message = service.retry(message.messageNo(),
                new IntegrationApplicationService.RetryCommand(message.version(), 1001L, "r1"));
        message = service.dispatch(message.messageNo(),
                new IntegrationApplicationService.DispatchCommand(false, "MQ timeout", message.version(), 1001L, "d2"));
        message = service.retry(message.messageNo(),
                new IntegrationApplicationService.RetryCommand(message.version(), 1001L, "r2"));
        message = service.dispatch(message.messageNo(),
                new IntegrationApplicationService.DispatchCommand(false, "MQ timeout", message.version(), 1001L, "d3"));
        IntegrationMapper.DeadLetterRow deadLetter = service.listDeadLetters().getFirst();
        IntegrationMapper.MessageRow replayed = service.replayDeadLetter(deadLetter.deadLetterNo(),
                new IntegrationApplicationService.ReplayCommand(1001L, "replay-1"));

        assertThat(message.status()).isEqualTo(IntegrationMessageAggregate.DEAD_LETTER);
        assertThat(replayed.status()).isEqualTo(IntegrationMessageAggregate.PENDING);
        assertThat(service.listDeadLetters().getFirst().replayed()).isTrue();
    }

    @Test
    void managesIntegrationEndpoints() {
        MemoryIntegrationMapper mapper = new MemoryIntegrationMapper();
        IntegrationApplicationService service = new IntegrationApplicationService(mapper);

        IntegrationMapper.EndpointRow endpoint = service.createEndpoint(
                new IntegrationApplicationService.CreateEndpointCommand("WMS", "HTTP",
                        "http://127.0.0.1:18080/events", 1000, 3, 1001L, "endpoint-1"));
        endpoint = service.disableEndpoint(endpoint.endpointNo(),
                new IntegrationApplicationService.DisableEndpointCommand(endpoint.version(), 1001L,
                        "endpoint-disable-1"));

        assertThat(endpoint.status()).isEqualTo(2);
        assertThat(service.listEndpoints()).hasSize(1);
    }

    public static class MemoryIntegrationMapper implements IntegrationMapper {
        final Map<String, RouteRow> routes = new LinkedHashMap<>();
        final Map<String, EndpointRow> endpoints = new LinkedHashMap<>();
        final Map<String, MessageRow> messages = new LinkedHashMap<>();
        final Map<String, DeadLetterRow> deadLetters = new LinkedHashMap<>();
        final List<DeliveryAttemptRow> deliveryAttempts = new ArrayList<>();
        final List<OperationLogRow> logs = new ArrayList<>();

        @Override
        public RouteRow findRoute(String routeNo) { return routes.get(routeNo); }

        @Override
        public List<RouteRow> findEnabledRoutes(String messageType, String sourceSystem) {
            return routes.values().stream()
                    .filter(row -> row.messageType().equals(messageType)
                            && row.sourceSystem().equals(sourceSystem)
                            && row.status() == 1)
                    .toList();
        }

        @Override
        public RouteRow findRouteForMessage(String messageType, String sourceSystem, String targetSystem) {
            return routes.values().stream()
                    .filter(row -> row.messageType().equals(messageType)
                            && row.sourceSystem().equals(sourceSystem)
                            && row.targetSystem().equals(targetSystem)
                            && row.status() == 1)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<RouteRow> listRoutes() { return new ArrayList<>(routes.values()); }

        @Override
        public EndpointRow findEnabledEndpoint(String targetSystem, String channelType) {
            return endpoints.values().stream()
                    .filter(row -> row.targetSystem().equals(targetSystem)
                            && row.channelType().equals(channelType)
                            && row.status() == 1)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public EndpointRow findEndpoint(String endpointNo) { return endpoints.get(endpointNo); }

        @Override
        public List<EndpointRow> listEndpoints() { return new ArrayList<>(endpoints.values()); }

        @Override
        public void insertEndpoint(EndpointRow row) { endpoints.put(row.endpointNo(), row); }

        @Override
        public void updateEndpoint(EndpointRow row) { endpoints.put(row.endpointNo(), row); }

        @Override
        public void insertRoute(RouteRow row) { routes.put(row.routeNo(), row); }

        @Override
        public void updateRoute(RouteRow row) { routes.put(row.routeNo(), row); }

        @Override
        public MessageRow findMessage(String messageNo) { return messages.get(messageNo); }

        @Override
        public MessageRow findMessageByIdempotency(String sourceSystem, String idempotencyKey) {
            return messages.values().stream()
                    .filter(row -> row.sourceSystem().equals(sourceSystem)
                            && row.idempotencyKey().equals(idempotencyKey))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<MessageRow> listMessages(Integer status) {
            return messages.values().stream()
                    .filter(row -> status == null || row.status() == status)
                    .toList();
        }

        @Override
        public List<MessageRow> listDispatchableMessages(int limit) {
            return messages.values().stream()
                    .filter(row -> row.status() == IntegrationMessageAggregate.PENDING
                            || row.status() == IntegrationMessageAggregate.FAILED)
                    .limit(limit)
                    .toList();
        }

        @Override
        public void insertMessage(MessageRow row) { messages.put(row.messageNo(), row); }

        @Override
        public void updateMessage(MessageRow row) { messages.put(row.messageNo(), row); }

        @Override
        public void insertDeadLetter(DeadLetterRow row) { deadLetters.put(row.deadLetterNo(), row); }

        @Override
        public DeadLetterRow findDeadLetter(String deadLetterNo) { return deadLetters.get(deadLetterNo); }

        @Override
        public List<DeadLetterRow> listDeadLetters() { return new ArrayList<>(deadLetters.values()); }

        @Override
        public void updateDeadLetter(DeadLetterRow row) { deadLetters.put(row.deadLetterNo(), row); }

        @Override
        public void insertOperationLog(OperationLogRow row) { logs.add(row); }

        @Override
        public List<OperationLogRow> listOperationLogs() { return logs; }

        @Override
        public void insertDeliveryAttempt(DeliveryAttemptRow row) {
            deliveryAttempts.add(row);
        }

        @Override
        public List<DeliveryAttemptRow> listDeliveryAttempts(String messageNo) {
            return deliveryAttempts.stream()
                    .filter(row -> messageNo == null || row.messageNo().equals(messageNo))
                    .toList();
        }

        @Override
        public DispatchSummaryRow dispatchSummary() {
            return new DispatchSummaryRow(count(IntegrationMessageAggregate.PENDING),
                    count(IntegrationMessageAggregate.DISPATCHED),
                    count(IntegrationMessageAggregate.FAILED),
                    count(IntegrationMessageAggregate.DEAD_LETTER),
                    count(IntegrationMessageAggregate.REPLAYED));
        }

        private long count(int status) {
            return messages.values().stream().filter(row -> row.status() == status).count();
        }
    }
}
