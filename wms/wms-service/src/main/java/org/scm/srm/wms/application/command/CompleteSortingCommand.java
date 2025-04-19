package org.scm.srm.wms.application.command;

import jakarta.validation.constraints.NotNull;

public record CompleteSortingCommand(
        @NotNull Long id
) {}