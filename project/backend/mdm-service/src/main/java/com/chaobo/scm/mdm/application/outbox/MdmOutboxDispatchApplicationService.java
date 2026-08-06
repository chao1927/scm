package com.chaobo.scm.mdm.application.outbox;

import com.chaobo.scm.mdm.infrastructure.persistence.MdmOutboxMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 主数据 Outbox 可靠投递应用服务。
 *
 * @author SCM Team
 */
@Service
@Profile("!test")
public class MdmOutboxDispatchApplicationService {

    private static final int MAX_BATCH_SIZE = 200;
    private static final int MAX_ERROR_LENGTH = 1000;
    private final MdmOutboxMapper mapper;
    private final MdmMessageBrokerPort broker;
    private final String defaultTopic;

    public MdmOutboxDispatchApplicationService(
            MdmOutboxMapper mapper, MdmMessageBrokerPort broker,
            @Value("${scm.rocketmq.mdm-producer.default-topic:mdm-domain-event}")
            String defaultTopic) {
        this.mapper = mapper;
        this.broker = broker;
        this.defaultTopic = defaultTopic;
    }

    /**
     * 投递一批事件，只在 Broker 确认后标记成功。
     *
     * @param limit 批次数量
     * @param maxRetries 最大重试次数
     * @return 投递统计
     */
    public DispatchResult dispatch(int limit, int maxRetries) {
        int published = 0;
        int failed = 0;
        for (var event : mapper.pending(Math.max(1, Math.min(limit, MAX_BATCH_SIZE)),
                Math.max(1, maxRetries))) {
            try {
                String topic = event.destinationTopic() == null
                    || event.destinationTopic().isBlank()
                    ? defaultTopic : event.destinationTopic();
                broker.publish(new MdmMessageBrokerPort.OutboundMessage(
                    Long.toString(event.eventId()), event.eventType(), event.businessNo(),
                    event.payload(), topic));
                mapper.markPublished(event.eventId());
                published++;
            } catch (RuntimeException exception) {
                mapper.markFailed(event.eventId(), error(exception));
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
        return value.length() <= MAX_ERROR_LENGTH
            ? value : value.substring(0, MAX_ERROR_LENGTH);
    }

    /** 投递结果。 */
    public record DispatchResult(int published, int failed) {
    }
}
