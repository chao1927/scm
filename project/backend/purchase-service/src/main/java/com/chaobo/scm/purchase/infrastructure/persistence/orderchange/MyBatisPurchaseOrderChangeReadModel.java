package com.chaobo.scm.purchase.infrastructure.persistence.orderchange;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.orderchange.*;
import com.chaobo.scm.purchase.domain.orderchange.PurchaseOrderChangeStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisPurchaseOrderChangeReadModel implements PurchaseOrderChangeReadModelPort {
    private final PurchaseOrderChangeMapper mapper;

    public MyBatisPurchaseOrderChangeReadModel(PurchaseOrderChangeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResult<PurchaseOrderChangeView> page(String orderNo, Integer status, int pageNo, int pageSize) {
        var total = mapper.count(orderNo, status);
        var records = mapper.page(orderNo, status, (pageNo - 1) * pageSize, pageSize)
                .stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<PurchaseOrderChangeView> detail(String changeNo) {
        return Optional.ofNullable(mapper.findByNo(changeNo)).map(this::view);
    }

    private PurchaseOrderChangeView view(PurchaseOrderChangeMapper.ChangeRow row) {
        var status = PurchaseOrderChangeStatus.of(row.status());
        return new PurchaseOrderChangeView(row.id(), row.changeNo(), row.orderNo(), row.changeType(),
                row.beforeSnapshot(), row.afterSnapshot(), row.changeReason(), row.status(), status.label(),
                row.version());
    }
}
