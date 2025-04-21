package org.scm.bdp.service.application.converter;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.Supplier;
import org.scm.bdp.service.application.command.supplier.CreateSupplierCommand;
import org.scm.bdp.service.domain.model.SupplierAgg;

public class SupplierConverter {

    public static SupplierAgg cmdConvertAgg(CreateSupplierCommand cmd) {
        Supplier supplier = new Supplier();
        supplier.setName(cmd.name());
        supplier.setCategoryId(cmd.categoryId());
        supplier.setContactPerson(cmd.contactPerson());
        supplier.setContactPhone(cmd.contactPhone());
        supplier.setAddress(cmd.address());
        supplier.setBusinessLicenseNumber(cmd.businessLicenseNumber());
        supplier.setBusinessLicensePhoto(cmd.businessLicensePhoto());
        supplier.setOrganizationCode(cmd.organizationCode());
        supplier.setStatus(SwitchStatus.DISABLED.getValue());
        return new SupplierAgg(supplier);
    }

}
