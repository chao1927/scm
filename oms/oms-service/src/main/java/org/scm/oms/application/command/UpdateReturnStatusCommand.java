package org.scm.oms.application.command;

public record UpdateReturnStatusCommand(
        String salesReturnNo,
        Integer status
) {}