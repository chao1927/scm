package org.scm.bdp.service.application.event;

import org.scm.common.DomainEvent;

public record LogisticsChannelCreatedEvent(
        Long id,
        String name
) implements DomainEvent {
    @Override
    public String topic() {
        return "logistics-channel-topic";
    }

    @Override
    public String type() {
        return "LogisticsChannelCreated";
    }
}