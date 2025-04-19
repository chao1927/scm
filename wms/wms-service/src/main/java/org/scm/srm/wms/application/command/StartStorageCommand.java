package org.scm.srm.wms.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartStorageCommand(@NotBlank String storageNo, @NotNull Long operatorEmpId) {}
