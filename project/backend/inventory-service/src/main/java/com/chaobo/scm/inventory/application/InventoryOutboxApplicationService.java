package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.infrastructure.persistence.InventoryEventMapper;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * 库存领域事件 Outbox 写入服务。
 *
 * <p>该服务只在本地业务事务内追加事件，不直接调用 RocketMQ；投递任务异步读取 Outbox 并发送。
 *
 * @author SCM Team
 */
@Service
public class InventoryOutboxApplicationService implements InventoryEventPublisher {

    private final InventoryEventMapper events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public InventoryOutboxApplicationService(InventoryEventMapper events) {
        this.events = events;
    }

    @Override
    public void publish(
            String eventType,
            String aggregateType,
            String aggregateId,
            String payloadJson) {
        long id = ids.incrementAndGet();
        events.insertOutbox(
                id,
                "INV-" + eventType + "-" + id,
                eventType,
                aggregateType,
                aggregateId,
                payloadJson);
    }
}
