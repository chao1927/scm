package org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.CreateAddressCommand;
import org.scm.bdp.service.application.command.DeleteAddressCommand;
import org.scm.bdp.service.application.command.UpdateAddressCommand;
import org.scm.bdp.service.application.converter.AddressConverter;
import org.scm.bdp.service.domain.model.AddressAgg;
import org.scm.bdp.service.domain.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddressCommandHandler {

    @Autowired
    private AddressRepository addressRepository;
    public void handle(CreateAddressCommand command) {
        AddressAgg addressAgg = AddressConverter.cmdConvertAgg(command);

        // TODO 设置经纬度

        addressRepository.save(addressAgg);
    }

    public void handle(UpdateAddressCommand command) {
        AddressAgg addressAgg = addressRepository.findById(command.id());
        addressAgg.update(command.province(), command.city(), command.district(), command.detailedAddress());
        // TODO 设置经纬度

        addressRepository.save(addressAgg);
    }

    public void handle(DeleteAddressCommand command) {
        addressRepository.checkExistById(command.id());
        addressRepository.deleteById(command.id());
    }
}
