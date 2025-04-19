package org.scm.bdp.service.domain.model;

import org.scm.bdp.service.adapter.infra.domain.Address;

public record AddressAgg(Address address) {

    public Long id() {
        return address.getId();
    }

}
