package com.chaobo.scm.oms.application;

import java.math.BigDecimal;

/**
 * 标准 V1 信封还原后的 OMS 外部业务事件。
 */
public record OmsExternalEvent(
        String sourceSystem,
        String eventCode,
        String eventType,
        String businessNo,
        String fulfillmentNo,
        String reservationRefNo,
        String reservationNo,
        BigDecimal quantity,
        String outboundNo,
        String wmsOrderNo,
        String afterSaleNo,
        String reason,
        BigDecimal receivedQty,
        BigDecimal acceptedQty,
        BigDecimal amount,
        boolean unmatched,
        String payload) {
}
