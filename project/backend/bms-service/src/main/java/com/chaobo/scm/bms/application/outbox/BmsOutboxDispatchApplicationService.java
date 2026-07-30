package com.chaobo.scm.bms.application.outbox;

import com.chaobo.scm.bms.infrastructure.persistence.BmsOutboxMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * BMS Outbox 可靠投递服务。
 *
 * @author SCM Team
 */
@Service
@Profile("!test")
public class BmsOutboxDispatchApplicationService {

    private final BmsOutboxMapper mapper;
    private final BmsMessageBrokerPort broker;

    public BmsOutboxDispatchApplicationService(BmsOutboxMapper mapper,
                                               BmsMessageBrokerPort broker) {
        this.mapper = mapper;
        this.broker = broker;
    }

    public DispatchResult dispatch(int limit, int maxRetries) {
        int published = 0;
        int failed = 0;
        for (var event : mapper.pending(
                Math.max(1, Math.min(limit, 200)), Math.max(1, maxRetries))) {
            try {
                broker.publish(new BmsMessageBrokerPort.OutboundMessage(
                    event.eventNo(), event.eventType(), event.aggregateNo(),
                    event.businessNo(), event.payload()));
                mapper.markPublished(event.eventNo());
                published++;
            } catch (RuntimeException exception) {
                mapper.markFailed(event.eventNo(), error(exception));
                failed++;
            }
        }
        return new DispatchResult(published, failed);
    }

    private static String error(RuntimeException exception) {
        String value = exception.getMessage();
        if (value == null || value.isBlank()) {
            value = exception.getClass().getSimpleName();
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    public record DispatchResult(int published, int failed) {
    }
}
