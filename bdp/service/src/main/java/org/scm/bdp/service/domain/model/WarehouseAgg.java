package org.scm.bdp.service.domain.model;

import lombok.AllArgsConstructor;
import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Address;
import org.scm.bdp.service.adapter.infra.domain.StorageLocation;
import org.scm.bdp.service.adapter.infra.domain.Warehouse;
import org.scm.bdp.service.application.command.warehouse.AddLocationToWarehouseCommand;
import org.scm.bdp.service.application.command.warehouse.CreateWarehouseCommand;
import org.scm.bdp.service.application.command.warehouse.UpdateLocationCommand;
import org.scm.bdp.service.application.command.warehouse.UpdateWarehouseCommand;

import java.util.List;

@AllArgsConstructor
public record WarehouseAgg(Warehouse warehouse, Address address, List<StorageLocation> storageLocations) {

    public static WarehouseAgg create(CreateWarehouseCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public Long id() {
        return warehouse.getId();
    }

    public void enable() {
        warehouse.setStatus(SwitchStatus.ENABLED.getValue());
    }

    public void disable() {
        warehouse.setStatus(SwitchStatus.DISABLED.getValue());
    }

    public void addStorageLocation(StorageLocation storageLocation) {
        storageLocation.setWarehouseId(warehouse.getId());
        storageLocations.add(storageLocation);
    }


    public void update(UpdateWarehouseCommand command) {
        // TODO 实现更新逻辑
    }

    public void delete() {
        // TODO 实现删除逻辑
    }

    public void addLocation(AddLocationToWarehouseCommand command) {
        // TODO 实现添加库位逻辑
    }

    public Long getLastAddedLocationId() {
        // TODO 实现获取最后一个添加的库位ID的逻辑
        return null;
    }

    public void updateLocation(UpdateLocationCommand command) {
        // TODO 实现更新库位逻辑
    }

    public void disableLocation(Long locationId) {
        // TODO 实现禁用库位逻辑
    }

    public void enableLocation(Long locationId) {
        // TODO 实现启用库位逻辑
    }

    public void deleteLocation(Long locationId) {
        // TODO 实现删除库位逻辑
    }
}
