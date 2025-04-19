package org.scm.srm.wms.application.command;

public record UnlockInventoryCommand(Long warehouseId, String sku, Integer quantity) {}
