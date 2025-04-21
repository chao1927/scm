package org.scm.bdp.service.application.command.warehouse;

import jakarta.validation.constraints.NotNull;

public record DeleteLocationCommand(@NotNull Long locationId) {}