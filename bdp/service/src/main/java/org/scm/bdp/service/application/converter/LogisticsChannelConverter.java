package org.scm.bdp.service.application.converter;

import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.bdp.service.adapter.infra.domain.LogisticsChannel;
import org.scm.bdp.service.application.command.CreateLogisticsChannelCommand;
import org.scm.bdp.service.domain.model.LogisticsChannelAgg;

public class LogisticsChannelConverter {

    public static LogisticsChannelAgg cmdConvertAgg(CreateLogisticsChannelCommand cmd) {
        LogisticsChannel logisticsChannel = new LogisticsChannel();
        logisticsChannel.setName(cmd.name());
        logisticsChannel.setServiceType(cmd.serviceType());
        logisticsChannel.setCoverageArea(cmd.coverageArea());
        logisticsChannel.setFreightCalculationRules(cmd.freightCalculationRules());
        logisticsChannel.setStatus(SwitchStatus.DISABLED.getValue());
        return new LogisticsChannelAgg(logisticsChannel);
    }

}
