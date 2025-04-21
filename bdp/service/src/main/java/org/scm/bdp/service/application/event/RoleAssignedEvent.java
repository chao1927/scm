package org.scm.bdp.service.application.event;

import org.scm.common.DomainEvent;

import java.util.List;

public record RoleAssignedEvent(Long userId, List<Long> roleIds) implements DomainEvent {
    public String topic() { return "auth-topic"; }
    public String type() { return "RoleAssigned"; }
}