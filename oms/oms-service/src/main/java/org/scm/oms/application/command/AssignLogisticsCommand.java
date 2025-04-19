package org.scm.oms.application.command;

public record AssignLogisticsCommand(
        String deliveryNo,
        String logisticNo,
        Long logisticChannelId
) {}