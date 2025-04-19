package org.scm.tms.application.command;

public record UpdateTrackingStatusCommand(
    String logisticsNo,
    Integer logisticStatus
) {}