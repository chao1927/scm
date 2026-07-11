package com.chaobo.scm.purchase.application.inbound;

import java.math.BigDecimal;

public record InboundTrackingView(long id, String inboundNo, String orderNo, String asnNo, long supplierId,
                                  long purchaseOrgId, String warehouseCode, String skuCode, BigDecimal notifiedQty,
                                  BigDecimal receivedQty, BigDecimal qualifiedQty, BigDecimal unqualifiedQty,
                                  BigDecimal putawayQty, int status, String statusName, String exceptionReason,
                                  int version, boolean fresh) {
}
