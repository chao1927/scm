package org.scm.oms.application.command;

public record UpdateDeliveryStatusCommand(
        String deliveryNo,
        Integer newStatus
) {}