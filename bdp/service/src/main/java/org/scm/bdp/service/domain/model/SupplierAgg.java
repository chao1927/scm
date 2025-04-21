package org.scm.bdp.service.domain.model;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Supplier;
import org.scm.bdp.service.application.command.supplier.CreateSupplierCommand;
import org.scm.bdp.service.application.command.supplier.UpdateSupplierCommand;

public record SupplierAgg(Supplier supplier) {

    public Long id() {
        return supplier.getId();
    }

    public void enable() {
        supplier.setStatus(SwitchStatus.ENABLED.getValue());
    }

    public void disable() {
        supplier.setStatus(SwitchStatus.DISABLED.getValue());
    }

    public void update(UpdateSupplierCommand command) {


    }

    public void delete() {

    }
}
