package org.scm.srm.wms.application.command;

import jakarta.validation.constraints.NotNull;

public record StartSortingCommand(
        @NotNull Long id
) {}