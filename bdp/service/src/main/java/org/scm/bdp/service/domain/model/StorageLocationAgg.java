package org.scm.bdp.service.domain.model;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.StorageLocation;

import java.math.BigDecimal;

public record StorageLocationAgg(StorageLocation location) {
    public Long id() {
        return location.getId();
    }

    public void update(Long warehouseId, String code, BigDecimal maxVolume, BigDecimal maxWeight, Integer mixingStrategy) {

        location.setWarehouseId(warehouseId);
        location.setCode(code);
        location.setMaxVolume(maxVolume);
        location.setMaxWeight(maxWeight);
        location.setMixingStrategy(mixingStrategy);
    }

    public void disable() {
        location.setStatus(SwitchStatus.DISABLED.getValue());
    }

    public void enable() {
        location.setStatus(SwitchStatus.ENABLED.getValue());
    }
}
