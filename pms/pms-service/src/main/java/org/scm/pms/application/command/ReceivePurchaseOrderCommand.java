package org.scm.pms.application.command;

import jakarta.validation.constraints.NotNull;

public record ReceivePurchaseOrderCommand(@NotNull Long id) {}
