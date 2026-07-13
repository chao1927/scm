package com.chaobo.scm.integration.application;

import com.chaobo.scm.integration.domain.IntegrationMessageAggregate;
import com.chaobo.scm.integration.domain.IntegrationRouteAggregate;
import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IntegrationApplicationService {
    private static final int MAX_RETRY = 3;

    private final IntegrationMapper mapper;
    private final AtomicLong routeSequence = new AtomicLong(100000);
    private final AtomicLong messageSequence = new AtomicLong(200000);
    private final AtomicLong deadLetterSequence = new AtomicLong(300000);
    private final AtomicLong endpointSequence = new AtomicLong(500000);

    public IntegrationApplicationService(IntegrationMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public IntegrationMapper.RouteRow createRoute(CreateRouteCommand command) {
        IntegrationRouteAggregate aggregate = IntegrationRouteAggregate.create("IR" + routeSequence.incrementAndGet(),
                command.messageType(), command.sourceSystem(), command.targetSystem(), command.channelType());
        IntegrationMapper.RouteRow row = toRow(aggregate);
        mapper.insertRoute(row);
        log("CREATE_ROUTE", row.routeNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public IntegrationMapper.RouteRow disableRoute(String routeNo, DisableRouteCommand command) {
        IntegrationRouteAggregate aggregate = loadRoute(routeNo);
        aggregate.disable(command.expectedVersion());
        mapper.updateRoute(toRow(aggregate));
        log("DISABLE_ROUTE", routeNo, command.operatorId(), command.idempotencyKey());
        return mapper.findRoute(routeNo);
    }

    public List<IntegrationMapper.RouteRow> listRoutes() {
        return mapper.listRoutes();
    }

    @Transactional
    public IntegrationMapper.EndpointRow createEndpoint(CreateEndpointCommand command) {
        validateEndpoint(command.targetSystem(), command.channelType(), command.endpointUrl(),
                command.timeoutMillis(), command.failureThreshold());
        IntegrationMapper.EndpointRow row = new IntegrationMapper.EndpointRow(null,
                "IE" + endpointSequence.incrementAndGet(), command.targetSystem(), command.channelType(),
                command.endpointUrl(), command.timeoutMillis(), command.failureThreshold(), 0, 1, 1);
        mapper.insertEndpoint(row);
        log("CREATE_ENDPOINT", row.endpointNo(), command.operatorId(), command.idempotencyKey());
        return row;
    }

    @Transactional
    public IntegrationMapper.EndpointRow disableEndpoint(String endpointNo, DisableEndpointCommand command) {
        IntegrationMapper.EndpointRow row = mapper.findEndpoint(endpointNo);
        if (row == null) {
            throw new IllegalArgumentException("integration endpoint not found");
        }
        if (row.version() != command.expectedVersion()) {
            throw new IllegalStateException("integration endpoint version conflict");
        }
        IntegrationMapper.EndpointRow disabled = new IntegrationMapper.EndpointRow(row.id(), row.endpointNo(),
                row.targetSystem(), row.channelType(), row.endpointUrl(), row.timeoutMillis(),
                row.failureThreshold(), row.consecutiveFailures(), 2, row.version() + 1);
        mapper.updateEndpoint(disabled);
        log("DISABLE_ENDPOINT", endpointNo, command.operatorId(), command.idempotencyKey());
        return mapper.findEndpoint(endpointNo);
    }

    public List<IntegrationMapper.EndpointRow> listEndpoints() {
        return mapper.listEndpoints();
    }

    @Transactional
    public List<IntegrationMapper.MessageRow> acceptEvent(AcceptMessageCommand command) {
        IntegrationMapper.MessageRow existing = mapper.findMessageByIdempotency(command.sourceSystem(),
                command.idempotencyKey());
        if (existing != null) {
            return List.of(existing);
        }
        List<IntegrationMapper.RouteRow> routes = mapper.findEnabledRoutes(command.messageType(),
                command.sourceSystem());
        if (routes.isEmpty()) {
            IntegrationMapper.MessageRow deadMessage = createMessage(command, "UNROUTED");
            markDeadLetter(deadMessage, "route not configured");
            return List.of(mapper.findMessage(deadMessage.messageNo()));
        }
        List<IntegrationMapper.MessageRow> rows = new ArrayList<>();
        for (IntegrationMapper.RouteRow route : routes) {
            IntegrationMapper.MessageRow row = createMessage(command, route.targetSystem());
            rows.add(row);
        }
        log("ACCEPT_INTEGRATION_MESSAGE", command.businessNo(), command.operatorId(), command.idempotencyKey());
        return rows;
    }

    @Transactional
    public IntegrationMapper.MessageRow dispatch(String messageNo, DispatchCommand command) {
        IntegrationMessageAggregate aggregate = loadMessage(messageNo);
        if (command.success()) {
            aggregate.markDispatched(command.expectedVersion());
            mapper.updateMessage(toRow(aggregate));
            log("DISPATCH_MESSAGE", messageNo, command.operatorId(), command.idempotencyKey());
            return mapper.findMessage(messageNo);
        }
        boolean deadLetter = aggregate.markFailed(command.failureReason(), MAX_RETRY, command.expectedVersion());
        mapper.updateMessage(toRow(aggregate));
        if (deadLetter) {
            markDeadLetter(mapper.findMessage(messageNo), command.failureReason());
        }
        log("DISPATCH_MESSAGE_FAILED", messageNo, command.operatorId(), command.idempotencyKey());
        return mapper.findMessage(messageNo);
    }

    @Transactional
    public IntegrationMapper.MessageRow retry(String messageNo, RetryCommand command) {
        IntegrationMessageAggregate aggregate = loadMessage(messageNo);
        aggregate.retry(command.expectedVersion());
        mapper.updateMessage(toRow(aggregate));
        log("RETRY_MESSAGE", messageNo, command.operatorId(), command.idempotencyKey());
        return mapper.findMessage(messageNo);
    }

    @Transactional
    public IntegrationMapper.MessageRow replayDeadLetter(String deadLetterNo, ReplayCommand command) {
        IntegrationMapper.DeadLetterRow dead = mapper.findDeadLetter(deadLetterNo);
        if (dead == null) {
            throw new IllegalArgumentException("dead letter not found");
        }
        if (dead.replayed()) {
            throw new IllegalStateException("dead letter already replayed");
        }
        IntegrationMapper.MessageRow replayed = new IntegrationMapper.MessageRow(null,
                "IM" + messageSequence.incrementAndGet(), dead.messageType(), dead.sourceSystem(),
                dead.targetSystem(), dead.businessNo(), command.idempotencyKey(), dead.payload(),
                IntegrationMessageAggregate.PENDING, 0, null, 1);
        mapper.insertMessage(replayed);
        mapper.updateDeadLetter(new IntegrationMapper.DeadLetterRow(null, dead.deadLetterNo(), dead.messageNo(),
                dead.messageType(), dead.sourceSystem(), dead.targetSystem(), dead.businessNo(), dead.payload(),
                dead.failureReason(), true));
        IntegrationMessageAggregate original = loadMessage(dead.messageNo());
        original.markReplayed(original.version());
        mapper.updateMessage(toRow(original));
        log("REPLAY_DEAD_LETTER", deadLetterNo, command.operatorId(), command.idempotencyKey());
        return replayed;
    }

    public List<IntegrationMapper.MessageRow> listMessages(Integer status) {
        return mapper.listMessages(status);
    }

    public List<IntegrationMapper.DeadLetterRow> listDeadLetters() {
        return mapper.listDeadLetters();
    }

    private IntegrationMapper.MessageRow createMessage(AcceptMessageCommand command, String targetSystem) {
        IntegrationMessageAggregate aggregate = IntegrationMessageAggregate.create("IM" + messageSequence.incrementAndGet(),
                command.messageType(), command.sourceSystem(), targetSystem, command.businessNo(),
                command.idempotencyKey(), command.payload());
        IntegrationMapper.MessageRow row = toRow(aggregate);
        mapper.insertMessage(row);
        return row;
    }

    private void markDeadLetter(IntegrationMapper.MessageRow row, String reason) {
        IntegrationMapper.DeadLetterRow deadLetter = new IntegrationMapper.DeadLetterRow(null,
                "DL" + deadLetterSequence.incrementAndGet(), row.messageNo(), row.messageType(), row.sourceSystem(),
                row.targetSystem(), row.businessNo(), row.payload(), reason, false);
        mapper.insertDeadLetter(deadLetter);
        IntegrationMessageAggregate aggregate = IntegrationMessageAggregate.restore(row.messageNo(), row.messageType(),
                row.sourceSystem(), row.targetSystem(), row.businessNo(), row.idempotencyKey(), row.payload(),
                IntegrationMessageAggregate.DEAD_LETTER, row.retryCount(), reason, row.version() + 1);
        mapper.updateMessage(toRow(aggregate));
    }

    private IntegrationRouteAggregate loadRoute(String routeNo) {
        IntegrationMapper.RouteRow row = mapper.findRoute(routeNo);
        if (row == null) {
            throw new IllegalArgumentException("integration route not found");
        }
        return IntegrationRouteAggregate.restore(row.routeNo(), row.messageType(), row.sourceSystem(),
                row.targetSystem(), row.channelType(), row.status(), row.version());
    }

    private IntegrationMessageAggregate loadMessage(String messageNo) {
        IntegrationMapper.MessageRow row = mapper.findMessage(messageNo);
        if (row == null) {
            throw new IllegalArgumentException("integration message not found");
        }
        return IntegrationMessageAggregate.restore(row.messageNo(), row.messageType(), row.sourceSystem(),
                row.targetSystem(), row.businessNo(), row.idempotencyKey(), row.payload(), row.status(),
                row.retryCount(), row.failureReason(), row.version());
    }

    private IntegrationMapper.RouteRow toRow(IntegrationRouteAggregate aggregate) {
        return new IntegrationMapper.RouteRow(null, aggregate.routeNo(), aggregate.messageType(),
                aggregate.sourceSystem(), aggregate.targetSystem(), aggregate.channelType(), aggregate.status(),
                aggregate.version());
    }

    private IntegrationMapper.MessageRow toRow(IntegrationMessageAggregate aggregate) {
        return new IntegrationMapper.MessageRow(null, aggregate.messageNo(), aggregate.messageType(),
                aggregate.sourceSystem(), aggregate.targetSystem(), aggregate.businessNo(),
                aggregate.idempotencyKey(), aggregate.payload(), aggregate.status(), aggregate.retryCount(),
                aggregate.failureReason(), aggregate.version());
    }

    private void log(String operationType, String businessNo, Long operatorId, String idempotencyKey) {
        mapper.insertOperationLog(new IntegrationMapper.OperationLogRow(operationType, businessNo, operatorId,
                idempotencyKey, LocalDateTime.now()));
    }

    private void validateEndpoint(String targetSystem, String channelType, String endpointUrl, int timeoutMillis,
                                  int failureThreshold) {
        if (targetSystem == null || targetSystem.isBlank()) {
            throw new IllegalArgumentException("target system is required");
        }
        if (channelType == null || channelType.isBlank()) {
            throw new IllegalArgumentException("channel type is required");
        }
        if (endpointUrl == null || endpointUrl.isBlank()) {
            throw new IllegalArgumentException("endpoint url is required");
        }
        if (timeoutMillis < 100 || timeoutMillis > 30000) {
            throw new IllegalArgumentException("timeout millis must be between 100 and 30000");
        }
        if (failureThreshold < 1 || failureThreshold > 20) {
            throw new IllegalArgumentException("failure threshold must be between 1 and 20");
        }
    }

    public record CreateRouteCommand(String messageType, String sourceSystem, String targetSystem,
                                     String channelType, Long operatorId, String idempotencyKey) {}
    public record DisableRouteCommand(long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record CreateEndpointCommand(String targetSystem, String channelType, String endpointUrl,
                                        int timeoutMillis, int failureThreshold, Long operatorId,
                                        String idempotencyKey) {}
    public record DisableEndpointCommand(long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record AcceptMessageCommand(String messageType, String sourceSystem, String businessNo,
                                       String idempotencyKey, String payload, Long operatorId) {}
    public record DispatchCommand(boolean success, String failureReason, long expectedVersion, Long operatorId,
                                  String idempotencyKey) {}
    public record RetryCommand(long expectedVersion, Long operatorId, String idempotencyKey) {}
    public record ReplayCommand(Long operatorId, String idempotencyKey) {}
}
