package org.scm.srm.wms.application.command;

public record IncreaseInventoryCommand(Long warehouseId, String sku, int quantity) {}
