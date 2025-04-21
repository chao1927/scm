package org.scm.bdp.service.application.event;

import org.scm.common.DomainEvent;

public record UserCreatedEvent(Long userId, String username) implements DomainEvent {
    public String topic() { return "user-topic"; }
    public String type() { return "UserCreated"; }
}