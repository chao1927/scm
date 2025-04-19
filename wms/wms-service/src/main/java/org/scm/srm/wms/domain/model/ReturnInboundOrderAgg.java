package org.scm.srm.wms.domain.model;

import org.scm.srm.wms._share.enums.ReturnInboundStatus;
import org.scm.srm.wms.adapter.infra.domain.ReturnInboundOrder;
import org.scm.srm.wms.application.command.ReceiveReturnInboundCommand;

import java.time.LocalDateTime;

public record ReturnInboundOrderAgg(ReturnInboundOrder inboundOrder) {

    public static ReturnInboundOrderAgg create(ReceiveReturnInboundCommand command) {
        ReturnInboundOrder entity = new ReturnInboundOrder();
        entity.setInboundNo(command.inboundNo());
        entity.setSalesReturnNo(command.salesReturnNo());
        entity.setOperatorId(command.operatorId());
        entity.setInboundStatus(ReturnInboundStatus.RECEIVING.getCode());
        entity.setLogisticsNo(command.logisticsNo());
        entity.setInboundTime(LocalDateTime.now());
        return new ReturnInboundOrderAgg(entity);
    }

    public void inspect() {
        inboundOrder.setInboundStatus(ReturnInboundStatus.INSPECTING.getCode());
    }

    public void complete() {
        inboundOrder.setInboundStatus(ReturnInboundStatus.COMPLETED.getCode());
        inboundOrder.setShelvingTime(LocalDateTime.now());
    }
}
