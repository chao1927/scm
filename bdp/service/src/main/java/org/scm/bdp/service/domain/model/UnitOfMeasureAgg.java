package org.scm.bdp.service.domain.model;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.UnitOfMeasure;

public record UnitOfMeasureAgg(UnitOfMeasure unitOfMeasure) {

    public Long id() {
        return unitOfMeasure.getId();
    }

    public void init() {
        unitOfMeasure.setStatus(SwitchStatus.DISABLED.getValue());
    }

    public void update(String name) {
        unitOfMeasure.setName(name);
    }

    public void enable() {
        unitOfMeasure.setStatus(SwitchStatus.ENABLED.getValue());
    }

    public void disable() {
        unitOfMeasure.setStatus(SwitchStatus.DISABLED.getValue());
    }
}
