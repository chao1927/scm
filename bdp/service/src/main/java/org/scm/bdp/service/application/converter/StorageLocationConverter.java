package org.scm.bdp.service.application.converter;

import org.scm.bdp.service.adapter.infra.domain.StorageLocation;
import org.scm.bdp.service.application.command.warehouse.CreateLocationCommand;
import org.scm.bdp.service.domain.model.StorageLocationAgg;

import org.scm.bdp.service._share.enums.SwitchStatus;

public class StorageLocationConverter {

    public static StorageLocationAgg cmdConvertAgg(CreateLocationCommand cmd) {
        StorageLocation storageLocation = new StorageLocation();
        storageLocation.setWarehouseId(cmd.warehouseId());
        storageLocation.setCode(cmd.code());
        storageLocation.setMaxVolume(cmd.maxVolume());
        storageLocation.setMaxWeight(cmd.maxWeight());
        storageLocation.setMixingStrategy(cmd.mixingStrategy());
        storageLocation.setStatus(SwitchStatus.DISABLED.getValue());

        return new StorageLocationAgg(storageLocation);
    }

}
