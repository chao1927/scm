package org.scm.tms.domain.model;

import org.scm.tms._share.enums.LogisticsStatus;
import org.scm.tms.adapter.infra.domain.LogisticsOrder;
import org.scm.tms.application.command.AssignLogisticsCommand;

public record LogisticsOrderAgg(LogisticsOrder logisticsOrder) {

    public static LogisticsOrderAgg create(AssignLogisticsCommand command) {
        // TODO 实现创建逻辑
        return null;
    }

    public void updateTrackingStatus(LogisticsStatus status) {
        this.logisticsOrder.setLogisticStatus(status.getCode());
    }

}
