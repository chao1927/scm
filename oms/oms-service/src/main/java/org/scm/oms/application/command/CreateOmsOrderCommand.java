package org.scm.oms.application.command;

import java.time.LocalDateTime;

public record CreateOmsOrderCommand(String omsOrderNo, String platform, LocalDateTime receiveTime) {}
