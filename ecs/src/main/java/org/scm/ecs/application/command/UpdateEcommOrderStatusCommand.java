package org.scm.ecs.application.command;

public record UpdateEcommOrderStatusCommand(
        Long id,
        Integer newStatus
) {}