package org.scm.oms.application.command;

public record UpdateOmsOrderStatusCommand(String omsOrderNo, int status) {}
