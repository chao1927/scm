package org.scm.srm.wms.application.command;

import jakarta.validation.constraints.NotBlank;

public record InspectStorageCommand(@NotBlank String storageNo) {}
