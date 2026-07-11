package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class MyBatisOutboxRepository implements OutboxRepository {
    private final EventPersistenceMapper mapper;

    public MyBatisOutboxRepository(EventPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void saveAll(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            mapper.insertOutbox(
                    event.eventCode(),
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.aggregateVersion(),
                    json(event),
                    event.occurredAt());
        }
    }

    private String json(DomainEvent event) {
        return event.payload().entrySet().stream()
                .map(this::entry)
                .collect(Collectors.joining(",", "{", "}"));
    }

    private String entry(Map.Entry<String, Object> entry) {
        return "\"" + escape(entry.getKey()) + "\":" + value(entry.getValue());
    }

    private String value(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
