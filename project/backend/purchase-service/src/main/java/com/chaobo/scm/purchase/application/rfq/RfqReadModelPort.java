package com.chaobo.scm.purchase.application.rfq;

import com.chaobo.scm.common.api.PageResult;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RfqReadModelPort {

    PageResult<RfqView> page(
            Long purchaseOrgId,
            Integer status,
            String categoryCode,
            Long supplierId,
            OffsetDateTime deadlineFrom,
            OffsetDateTime deadlineTo,
            int pageNo,
            int pageSize);

    Optional<RfqView> detail(long id);

    Optional<RfqView> detailByNo(String rfqNo);
}
