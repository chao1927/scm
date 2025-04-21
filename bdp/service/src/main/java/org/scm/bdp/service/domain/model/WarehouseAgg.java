package org.scm.bdp.service.domain.model;

import lombok.AllArgsConstructor;
import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Warehouse;

import java.math.BigDecimal;

@AllArgsConstructor
public record WarehouseAgg(Warehouse warehouse) {

    public Long id() {
        return warehouse.getId();
    }

    public void enable() {
        warehouse.setStatus(SwitchStatus.ENABLED.getValue());
    }

    public void disable() {
        warehouse.setStatus(SwitchStatus.DISABLED.getValue());
    }


    public void update(String name, String description, String address, Integer type, BigDecimal area, String manager, String managerPhone) {
        warehouse.setName(name);
        warehouse.setDescription(description);
        warehouse.setAddress(address);
        warehouse.setType(type);
        warehouse.setArea(area);
        warehouse.setManager(manager);
        warehouse.setManagerPhone(managerPhone);
    }

}
