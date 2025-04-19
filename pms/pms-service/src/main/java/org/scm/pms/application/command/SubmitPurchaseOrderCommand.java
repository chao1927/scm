package org.scm.pms.application.command;

import jakarta.validation.constraints.NotNull;

public record SubmitPurchaseOrderCommand(@NotNull Long id) {}
