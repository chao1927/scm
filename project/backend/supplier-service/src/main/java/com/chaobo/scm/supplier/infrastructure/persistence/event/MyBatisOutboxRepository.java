package com.chaobo.scm.supplier.infrastructure.persistence.event;

import com.chaobo.scm.supplier.application.shared.OutboxRepository;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MyBatisOutboxRepository implements OutboxRepository {
    private final EventPersistenceMapper mapper;
    private final ObjectMapper objectMapper;

    public MyBatisOutboxRepository(EventPersistenceMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveAll(List<DomainEvent> events) {
        events.forEach(event -> mapper.insertEvent(event.eventId(), event.eventCode(), event.eventName(),
                event.eventType(), event.aggregateType(), event.aggregateId(), event.aggregateNo(),
                toJson(event), event.occurredAt()));
    }

    private String toJson(DomainEvent event) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", event.eventCode());
        envelope.put("eventType", event.eventType());
        envelope.put("eventName", event.eventName());
        envelope.put("eventVersion", 1);
        envelope.put("sourceSystem", "SUPPLIER");
        envelope.put("aggregateType", event.aggregateType());
        envelope.put("aggregateId", event.aggregateId());
        envelope.put("aggregateNo", event.aggregateNo());
        envelope.put("aggregateVersion", event.aggregateVersion());
        envelope.put("operatorId", event.operatorId());
        envelope.put("occurredAt", event.occurredAt());
        envelope.put("data", event.payload());
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JacksonException exception) {
            throw new IllegalStateException("领域事件序列化失败", exception);
        }
    }
}
