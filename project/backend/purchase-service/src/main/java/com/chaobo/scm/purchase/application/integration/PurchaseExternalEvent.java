package com.chaobo.scm.purchase.application.integration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record PurchaseExternalEvent(
        String sourceSystem,
        String eventCode,
        String eventType,
        String businessNo,
        String orderNo,
        String rfqNo,
        String quoteNo,
        String inboundNo,
        String asnNo,
        Long supplierId,
        Long purchaseOrgId,
        String warehouseCode,
        String skuCode,
        BigDecimal quantity,
        BigDecimal receivedQty,
        BigDecimal qualifiedQty,
        BigDecimal unqualifiedQty,
        BigDecimal putawayQty,
        BigDecimal amount,
        String currency,
        String shipmentId,
        String waybillNo,
        String carrierCode,
        String transportNode,
        String status,
        String reason,
        Integer sourceVersion,
        OffsetDateTime occurredAt,
        Map<String, Object> payload) {
}
