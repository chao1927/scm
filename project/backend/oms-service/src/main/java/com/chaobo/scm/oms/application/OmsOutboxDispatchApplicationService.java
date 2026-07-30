package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.infrastructure.persistence.OmsOutboxMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * OMS Outbox 可靠投递应用服务。
 *
 * <p>每条记录先原子抢占，再发送至 RocketMQ；只有代理确认发送成功后才标记为
 * 已发布。发送失败会记录原因并回到可重试状态。超时的抢占记录由 Mapper 自动
 * 重新纳入候选集，避免进程崩溃导致永久卡死。
 */
@Service
@ConditionalOnProperty(name = "scm.rocketmq.enabled", havingValue = "true")
public class OmsOutboxDispatchApplicationService {

    private static final int MAX_ERROR_LENGTH = 512;
    private final OmsOutboxMapper mapper;
    private final OmsMessageBroker broker;

    public OmsOutboxDispatchApplicationService(OmsOutboxMapper mapper,
                                               OmsMessageBroker broker) {
        this.mapper = mapper;
        this.broker = broker;
    }

    /**
     * 扫描并投递一批待发布事件。
     *
     * @param requestedLimit 请求批量大小
     * @return 本批次处理结果
     */
    public DispatchResult dispatch(int requestedLimit) {
        int published = 0;
        int failed = 0;
        int limit = Math.max(1, Math.min(requestedLimit, 200));
        for (OmsOutboxMapper.OutboxMessage event : mapper.pending(limit)) {
            if (mapper.claim(event.id()) != 1) {
                continue;
            }
            try {
                broker.publish(new OmsMessageBroker.OutboundMessage(
                        event.eventCode(), event.eventType(),
                        event.businessNo(), event.payload()));
                mapper.markPublished(event.id());
                published++;
            } catch (RuntimeException exception) {
                mapper.markFailed(event.id(), errorMessage(exception));
                failed++;
            }
        }
        return new DispatchResult(published, failed);
    }

    private static String errorMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        return message.length() <= MAX_ERROR_LENGTH
                ? message : message.substring(0, MAX_ERROR_LENGTH);
    }

    /**
     * 一批 Outbox 的投递统计。
     *
     * @param published 已发布数
     * @param failed 失败数
     */
    public record DispatchResult(int published, int failed) {
    }
}
