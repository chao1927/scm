package org.scm.bdp.service.domain.model;

import org.scm.bdp.service.adapter.infra.domain.Address;

public record AddressAgg(Address address) {

    public Long id() {
        return address.getId();
    }

    public void update(String province, String city, String district, String detailedAddress) {
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailedAddress(detailedAddress);
    }
}
