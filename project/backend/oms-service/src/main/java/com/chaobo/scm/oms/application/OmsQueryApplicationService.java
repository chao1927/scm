package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.stereotype.Service;

@Service
public class OmsQueryApplicationService {
    private final OmsMapper omsMapper;
    private final FulfillmentMapper fulfillmentMapper;

    public OmsQueryApplicationService(OmsMapper omsMapper, FulfillmentMapper fulfillmentMapper) {
        this.omsMapper = omsMapper;
        this.fulfillmentMapper = fulfillmentMapper;
    }

    public ExternalOrderView order(String orderNo) {
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        FulfillmentMapper.FulfillmentRow fulfillment = fulfillmentMapper.findBySalesOrder(orderNo);
        FulfillmentMapper.OutboundRow outbound = fulfillment == null || fulfillment.outboundNo() == null
                ? null
                : fulfillmentMapper.findOutbound(fulfillment.outboundNo());
        return toView(order, fulfillment, outbound);
    }

    public ExternalOrderView fulfillment(String fulfillmentNo) {
        FulfillmentMapper.FulfillmentRow fulfillment = fulfillmentMapper.findFulfillment(fulfillmentNo);
        if (fulfillment == null) {
            throw new IllegalArgumentException("fulfillment not found");
        }
        OmsMapper.SalesOrderRow order = omsMapper.findOrder(fulfillment.salesOrderNo());
        if (order == null) {
            throw new IllegalArgumentException("sales order not found");
        }
        FulfillmentMapper.OutboundRow outbound = fulfillment.outboundNo() == null
                ? null
                : fulfillmentMapper.findOutbound(fulfillment.outboundNo());
        return toView(order, fulfillment, outbound);
    }

    private ExternalOrderView toView(OmsMapper.SalesOrderRow order,
                                     FulfillmentMapper.FulfillmentRow fulfillment,
                                     FulfillmentMapper.OutboundRow outbound) {
        return new ExternalOrderView(order.orderNo(), order.channelCode(), order.status(),
                fulfillment == null ? null : fulfillment.fulfillmentNo(),
                fulfillment == null ? null : fulfillment.status(),
                fulfillment == null ? null : fulfillment.warehouseCode(),
                fulfillment == null ? null : fulfillment.reservationRefNo(),
                outbound == null ? null : outbound.outboundNo(),
                outbound == null ? null : outbound.status(),
                outbound == null ? null : outbound.wmsOrderNo());
    }

    public record ExternalOrderView(String salesOrderNo, String channelCode, int salesOrderStatus,
                                    String fulfillmentNo, Integer fulfillmentStatus, String warehouseCode,
                                    String reservationRefNo, String outboundNo, Integer outboundStatus,
                                    String wmsOrderNo) {}
}
