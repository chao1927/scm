package org.scm.bdp.service.domain.model;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.LogisticsChannel;
import org.scm.bdp.service.application.command.CreateLogisticsChannelCommand;
import org.scm.bdp.service.application.command.UpdateLogisticsChannelCommand;

public record LogisticsChannelAgg(LogisticsChannel logisticsChannel) {

    public Long id() {
        return logisticsChannel.getId();
    }

    public String name() {
        return logisticsChannel.getName();
    }

    public void update(String name, Integer serviceType, String coverageArea, String freightCalculationRules) {
        logisticsChannel.setName(name);
        logisticsChannel.setServiceType(serviceType);
        logisticsChannel.setCoverageArea(coverageArea);
        logisticsChannel.setFreightCalculationRules(freightCalculationRules);
    }

    public void disable() {
        logisticsChannel.setStatus(SwitchStatus.DISABLED.getValue());
    }

    public void enable() {
        logisticsChannel.setStatus(SwitchStatus.ENABLED.getValue());
    }

}
