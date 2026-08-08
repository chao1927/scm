package com.chaobo.scm.tms.application.outbox;

import com.chaobo.scm.common.logging.ScmLogContext;
import com.chaobo.scm.tms.infrastructure.persistence.TmsOutboxMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TMS Outbox 可靠投递应用服务。
 *
 * <p>仅在 RocketMQ 确认发送成功后标记已发布；失败会保留原因和退避时间，等待调度重试。
 *
 * @author SCM Team
 */
@Service
@Profile("!test")
public class TmsOutboxDispatchApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(TmsOutboxDispatchApplicationService.class);

    private static final int MAX_ERROR_LENGTH = 1000;
    private final TmsOutboxMapper mapper;
    private final TmsMessageBrokerPort broker;

    public TmsOutboxDispatchApplicationService(TmsOutboxMapper mapper,
                                               TmsMessageBrokerPort broker) {
        this.mapper = mapper;
        this.broker = broker;
    }

    /**
     * 投递一批事件。
     *
     * @param limit 批量上限
     * @param maxRetries 最大失败次数
     * @return 成功和失败数量
     */
    public DispatchResult dispatch(int limit, int maxRetries) {
        int published = 0;
        int failed = 0;
        for (TmsOutboxMapper.OutboxEvent event :
                mapper.pending(Math.max(1, Math.min(limit, 200)), Math.max(1, maxRetries))) {
            try (ScmLogContext ignored = ScmLogContext.openSystem(event.eventCode())) {
                broker.publish(new TmsMessageBrokerPort.OutboundMessage(
                    event.eventCode(), event.eventType(), event.businessNo(), event.payload()));
                mapper.markPublished(event.id());
                published++;
                LOG.info("event=rocketmq_publish operation=tms_outbox_publish result=SUCCESS eventId={} eventCode={} eventType={}",
                        event.id(), event.eventCode(), event.eventType());
            } catch (RuntimeException exception) {
                mapper.markFailed(event.id(), errorMessage(exception));
                failed++;
                try (ScmLogContext ignored = ScmLogContext.openSystem(event.eventCode())) {
                    LOG.error("event=rocketmq_publish operation=tms_outbox_publish result=FAILURE eventId={} eventCode={} eventType={}",
                            event.id(), event.eventCode(), event.eventType(), exception);
                }
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

    public record DispatchResult(int published, int failed) {
    }
}
