package org.scm.srm.wms.domain.model;

import org.scm.srm.wms.adapter.infra.domain.SalesOutboundOrder;
import org.scm.srm.wms.application.command.CreateSalesOutboundOrderCommand;

public record SalesOutboundOrderAgg(SalesOutboundOrder order) {

    public static SalesOutboundOrderAgg create(CreateSalesOutboundOrderCommand command) {
        return null;
    }

    public void completeOutbound() {
        // TODO complete outbound
    }

}
