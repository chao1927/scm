package org.scm.srm.wms.application.command;

import jakarta.validation.constraints.NotNull;

public record AdjustLocationInventoryCommand(
        @NotNull Long id,
        @NotNull Integer newQuantity
) {}