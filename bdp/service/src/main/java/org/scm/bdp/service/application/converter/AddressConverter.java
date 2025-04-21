package org.scm.bdp.service.application.converter;

import org.scm.bdp.service.adapter.infra.domain.Address;
import org.scm.bdp.service.application.command.CreateAddressCommand;
import org.scm.bdp.service.domain.model.AddressAgg;

public class AddressConverter {

    public static AddressAgg cmdConvertAgg(CreateAddressCommand cmd) {
        Address address = new Address();
        address.setProvince(cmd.province());
        address.setCity(cmd.city());
        address.setDistrict(cmd.district());
        address.setDetailedAddress(cmd.detailedAddress());
        return new AddressAgg(address);

    }

}
