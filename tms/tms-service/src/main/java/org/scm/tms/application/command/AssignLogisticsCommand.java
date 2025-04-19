package org.scm.tms.application.command;

public record AssignLogisticsCommand(
    String logisticsNo,
    String senderName,
    String receiverName,
    Integer logisticType
) {}