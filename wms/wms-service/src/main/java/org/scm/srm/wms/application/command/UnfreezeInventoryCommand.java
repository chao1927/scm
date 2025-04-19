package org.scm.srm.wms.application.command;

public record UnfreezeInventoryCommand(Long warehouseId, String sku, Integer quantity) {}
