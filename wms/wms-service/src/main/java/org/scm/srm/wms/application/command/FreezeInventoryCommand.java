package org.scm.srm.wms.application.command;

public record FreezeInventoryCommand(Long warehouseId, String sku, Integer quantity) {}
