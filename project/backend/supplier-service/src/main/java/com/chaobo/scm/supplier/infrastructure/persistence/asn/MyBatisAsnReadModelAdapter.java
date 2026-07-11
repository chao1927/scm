package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.supplier.application.asn.AsnReadModelPort;
import com.chaobo.scm.supplier.application.asn.AsnSummaryView;
import com.chaobo.scm.supplier.domain.asn.AsnStatus;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAsnReadModelAdapter implements AsnReadModelPort {
    private final AsnQueryMapper mapper;

    public MyBatisAsnReadModelAdapter(AsnQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResult<AsnSummaryView> page(Long supplierId, Integer status, String keyword,
                                           int pageNo, int pageSize) {
        long total = mapper.count(supplierId, status, keyword);
        var records = mapper.page(supplierId, status, keyword, (pageNo - 1) * pageSize, pageSize)
                .stream().map(this::toView).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    private AsnSummaryView toView(AsnSummaryRow row) {
        int statusCode = row.asnStatus();
        AsnStatus status = AsnStatus.fromCode(statusCode);
        return new AsnSummaryView(row.asnId(), row.asnNo(), row.purchaseOrderId(), row.supplierId(),
                row.warehouseId(), row.eta(), statusCode, status.label(), row.version(), row.updatedAt());
    }
}
