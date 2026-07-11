package com.chaobo.scm.purchase.application.inbound;

import com.chaobo.scm.common.api.PageResult;

import java.util.Optional;

public interface InboundTrackingReadModelPort {
    PageResult<InboundTrackingView> page(Long purchaseOrgId, String orderNo, String asnNo, String warehouseCode,
                                         Integer status, int pageNo, int pageSize);
    Optional<InboundTrackingView> detail(String inboundNo);
}
