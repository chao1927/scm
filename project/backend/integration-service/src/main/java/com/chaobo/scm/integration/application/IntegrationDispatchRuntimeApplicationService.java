package com.chaobo.scm.integration.application;

import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IntegrationDispatchRuntimeApplicationService {
    private final IntegrationMapper mapper;
    private final IntegrationApplicationService integrationService;
    private final IntegrationTransportPort transportPort;
    private final AtomicLong attemptSequence = new AtomicLong(400000);

    public IntegrationDispatchRuntimeApplicationService(IntegrationMapper mapper,
                                                        IntegrationApplicationService integrationService,
                                                        IntegrationTransportPort transportPort) {
        this.mapper = mapper;
        this.integrationService = integrationService;
        this.transportPort = transportPort;
    }

    @Transactional
    public DispatchRunResult dispatchDueMessages(DispatchRunCommand command) {
        int limit = command.limit() == null || command.limit() <= 0 ? 50 : Math.min(command.limit(), 200);
        List<IntegrationMapper.MessageRow> messages = mapper.listDispatchableMessages(limit);
        List<IntegrationMapper.DeliveryAttemptRow> attempts = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        int deadLetterCount = 0;
        for (IntegrationMapper.MessageRow message : messages) {
            IntegrationMapper.RouteRow route = mapper.findRouteForMessage(message.messageType(),
                    message.sourceSystem(), message.targetSystem());
            IntegrationTransportPort.DeliveryResult result = deliver(message, route);
            IntegrationMapper.MessageRow dispatched = integrationService.dispatch(message.messageNo(),
                    new IntegrationApplicationService.DispatchCommand(result.success(), result.failureReason(),
                            message.version(), command.operatorId(), command.idempotencyKey()));
            IntegrationMapper.DeliveryAttemptRow attempt = new IntegrationMapper.DeliveryAttemptRow(
                    "DA" + attemptSequence.incrementAndGet(), message.messageNo(), message.messageType(),
                    message.sourceSystem(), message.targetSystem(),
                    route == null ? "UNROUTED" : route.channelType(), result.success(), result.failureReason(),
                    result.durationMillis(), LocalDateTime.now());
            mapper.insertDeliveryAttempt(attempt);
            attempts.add(attempt);
            if (result.success()) {
                successCount++;
            } else if (dispatched.status() == 4) {
                deadLetterCount++;
            } else {
                failedCount++;
            }
        }
        return new DispatchRunResult(messages.size(), successCount, failedCount, deadLetterCount, attempts);
    }

    public List<IntegrationMapper.DeliveryAttemptRow> listDeliveryAttempts(String messageNo) {
        return mapper.listDeliveryAttempts(messageNo);
    }

    public IntegrationMapper.DispatchSummaryRow dispatchSummary() {
        return mapper.dispatchSummary();
    }

    private IntegrationTransportPort.DeliveryResult deliver(IntegrationMapper.MessageRow message,
                                                           IntegrationMapper.RouteRow route) {
        if (route == null || route.status() != 1) {
            return IntegrationTransportPort.DeliveryResult.failed("enabled route not found", 0);
        }
        return transportPort.deliver(new IntegrationTransportPort.DeliveryRequest(message.messageNo(),
                message.messageType(), message.sourceSystem(), message.targetSystem(), route.channelType(),
                message.businessNo(), message.idempotencyKey(), message.payload()));
    }

    public record DispatchRunCommand(Integer limit, Long operatorId, String idempotencyKey) {}

    public record DispatchRunResult(int scannedCount, int successCount, int failedCount, int deadLetterCount,
                                    List<IntegrationMapper.DeliveryAttemptRow> attempts) {}
}
