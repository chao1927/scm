package org.scm.bdp.service.application.converter;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Warehouse;
import org.scm.bdp.service.application.command.warehouse.CreateWarehouseCommand;
import org.scm.bdp.service.domain.model.WarehouseAgg;


public class WarehouseConverter {

    public static WarehouseAgg cmdConvertAgg(CreateWarehouseCommand cmd) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(cmd.name());
        warehouse.setDescription(cmd.description());
        warehouse.setAddress(cmd.address());
        warehouse.setType(cmd.type());
        warehouse.setArea(cmd.area());
        warehouse.setManager(cmd.manager());
        warehouse.setManagerPhone(cmd.managerPhone());
        warehouse.setStatus(SwitchStatus.DISABLED.getValue());

        return new WarehouseAgg(warehouse);
    }

}
