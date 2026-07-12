package com.chaobo.scm.wms.application.outbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WmsOutboxDispatchApplicationService {
    private final WmsEventMapper mapper;
    private final WmsMessageBrokerPort broker;

    public WmsOutboxDispatchApplicationService(WmsEventMapper mapper, WmsMessageBrokerPort broker) {
        this.mapper = mapper;
        this.broker = broker;
    }

    @Transactional
    public DispatchResult dispatchPending(int limit) {
        int batchSize = limit <= 0 ? 50 : Math.min(limit, 200);
        int published = 0;
        int failed = 0;
        for (var event : mapper.pending(batchSize)) {
            try {
                broker.publish(event.code(), event.type(), event.payload());
                mapper.markPublished(event.id());
                published++;
            } catch (RuntimeException ex) {
                mapper.markFailed(event.id());
                failed++;
            }
        }
        return new DispatchResult(published, failed);
    }

    public List<EventView> failedEvents(int limit) {
        int batchSize = limit <= 0 ? 50 : Math.min(limit, 200);
        return mapper.failed(batchSize).stream().map(EventView::from).toList();
    }

    @Transactional
    public void retry(long eventId) {
        if (mapper.retry(eventId) != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "失败事件不存在或状态不可重试");
        }
    }

    public record DispatchResult(int published, int failed) {
    }

    public record EventView(
            long id,
            String code,
            String type,
            String aggregateType,
            String aggregateId,
            int version,
            int retryCount
    ) {
        static EventView from(WmsEventMapper.Row row) {
            return new EventView(
                    row.id(),
                    row.code(),
                    row.type(),
                    row.aggregateType(),
                    row.aggregateId(),
                    row.version(),
                    row.retryCount()
            );
        }
    }
}
