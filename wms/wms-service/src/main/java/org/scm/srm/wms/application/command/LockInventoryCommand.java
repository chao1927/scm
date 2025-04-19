package org.scm.srm.wms.application.command;

public record LockInventoryCommand(Long warehouseId, String sku, int quantity) {}
