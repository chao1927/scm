package org.scm.bdp.service.application.command;

import jakarta.validation.constraints.NotNull;

public record DeleteAddressCommand(
        @NotNull Long id
) {
}
