package org.scm.srm.domain.model;

import org.scm.common.exception.BizException;
import org.scm.srm._share.enums.DeliveryErrorCode;
import org.scm.srm._share.enums.SupplierDeliveryOrderStatus;
import org.scm.srm.adapter.infra.domain.SupplierDeliveryOrder;

import java.time.LocalDateTime;

public class SupplierDeliveryOrderAgg {

    private SupplierDeliveryOrder order;

    public SupplierDeliveryOrderAgg(SupplierDeliveryOrder order) {
        this.order = order;
    }

    public void dispatch(LocalDateTime deliveryTime, String logisticsNo) {
        if (order.getStatus() != SupplierDeliveryOrderStatus.PENDING.getCode()) {
            throw new BizException(DeliveryErrorCode.INVALID_STATUS);
        }
        order.setDeliveryTime(deliveryTime);
        order.setLogisticsNumber(logisticsNo);
        order.setStatus(SupplierDeliveryOrderStatus.IN_TRANSIT.getCode());
    }

    public void confirmArrival() {
        if (order.getStatus() != SupplierDeliveryOrderStatus.IN_TRANSIT.getCode()) {
            throw new BizException(DeliveryErrorCode.INVALID_STATUS);
        }
        order.setStatus(SupplierDeliveryOrderStatus.DELIVERED.getCode());
    }

    public SupplierDeliveryOrder getOrder() {
        return order;
    }
}
