package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import com.chaobo.scm.purchase.application.integration.InboundEventLogPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisInboundEventLogAdapter implements InboundEventLogPort {
    private final InboundEventLogMapper mapper;

    public MyBatisInboundEventLogAdapter(InboundEventLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ClaimResult claim(String sourceSystem, String eventCode, String eventType,
                             String consumerName, String idempotentKey) {
        if (mapper.insertProcessing(sourceSystem, eventCode, eventType, consumerName, idempotentKey) == 1) {
            return ClaimResult.CLAIMED;
        }
        var existing = mapper.find(sourceSystem, eventCode, consumerName);
        if (existing == null) {
            return ClaimResult.IN_PROGRESS;
        }
        if (existing.status() == 2 || existing.status() == 4) {
            return ClaimResult.ALREADY_SUCCEEDED;
        }
        if (existing.status() == 3 && mapper.retryFailed(sourceSystem, eventCode, consumerName) == 1) {
            return ClaimResult.CLAIMED;
        }
        return ClaimResult.IN_PROGRESS;
    }

    @Override
    public void savePayload(String sourceSystem, String eventCode, String consumerName, String payloadJson) {
        mapper.savePayload(sourceSystem, eventCode, consumerName, payloadJson);
    }

    @Override
    public void markSucceeded(String sourceSystem, String eventCode, String consumerName, boolean ignored) {
        mapper.markSucceeded(sourceSystem, eventCode, consumerName, ignored ? 4 : 2);
    }

    @Override
    public void recordFailure(String sourceSystem, String eventCode, String eventType,
                              String consumerName, String idempotentKey, String reason) {
        var safeReason = reason == null ? "未知消费异常" : reason;
        mapper.recordFailure(sourceSystem, eventCode, eventType, consumerName, idempotentKey,
                safeReason.length() > 1000 ? safeReason.substring(0, 1000) : safeReason);
    }

    @Override
    public Optional<ReplayEvent> findForReplay(long consumeLogId) {
        return Optional.ofNullable(mapper.findById(consumeLogId)).filter(event -> event.status() == 3);
    }

    @Override
    public void markReplayRequested(long consumeLogId, long operatorId, String reason) {
        mapper.markReplayRequested(consumeLogId, operatorId, reason);
    }
}
