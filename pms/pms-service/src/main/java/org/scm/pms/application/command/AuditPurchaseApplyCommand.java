package org.scm.pms.application.command;

import jakarta.validation.constraints.NotNull;

public record AuditPurchaseApplyCommand(
    @NotNull Long id,
    @NotNull Boolean approved,
    String reason
) {}