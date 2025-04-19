package org.scm.pms.application.command;

import jakarta.validation.constraints.NotNull;

public record CancelPurchaseOrderCommand(@NotNull Long id) {}
