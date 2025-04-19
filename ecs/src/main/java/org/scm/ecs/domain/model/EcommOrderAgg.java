package org.scm.ecs.domain.model;

import org.scm.ecs.adapter.infra.domain.EcommOrder;
import org.scm.ecs.application.command.UpdateEcommOrderStatusCommand;

public record EcommOrderAgg(EcommOrder order) {

    public void updateStatus(UpdateEcommOrderStatusCommand command) {
        order.setOrderStatus(command.newStatus());
    }

}
