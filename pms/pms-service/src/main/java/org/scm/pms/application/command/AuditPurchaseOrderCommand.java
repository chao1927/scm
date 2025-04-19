package org.scm.pms.application.command;

import jakarta.validation.constraints.NotNull;

public record AuditPurchaseOrderCommand(@NotNull Long id) {}
