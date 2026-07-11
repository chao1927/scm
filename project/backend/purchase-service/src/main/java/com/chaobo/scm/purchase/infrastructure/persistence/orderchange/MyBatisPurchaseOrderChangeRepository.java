package com.chaobo.scm.purchase.infrastructure.persistence.orderchange;

import com.chaobo.scm.purchase.domain.orderchange.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisPurchaseOrderChangeRepository implements PurchaseOrderChangeRepository {
    private final PurchaseOrderChangeMapper mapper;

    public MyBatisPurchaseOrderChangeRepository(PurchaseOrderChangeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<PurchaseOrderChangeAggregate> findByNo(String changeNo) {
        return Optional.ofNullable(mapper.findByNo(changeNo)).map(this::aggregate);
    }

    @Override
    public void save(PurchaseOrderChangeAggregate change, long operatorId) {
        var existed = mapper.findByNo(change.changeNo()) != null;
        if (existed) {
            mapper.updateStatus(change.id(), change.status().code(), change.version(), operatorId);
        } else {
            mapper.insert(new PurchaseOrderChangeMapper.ChangeRow(change.id(), change.changeNo(), change.orderNo(),
                    change.changeType(), change.beforeSnapshot(), change.afterSnapshot(), change.changeReason(),
                    change.status().code(), change.version()), operatorId);
        }
    }

    private PurchaseOrderChangeAggregate aggregate(PurchaseOrderChangeMapper.ChangeRow row) {
        return new PurchaseOrderChangeAggregate(row.id(), row.changeNo(), row.orderNo(), row.changeType(),
                row.beforeSnapshot(), row.afterSnapshot(), row.changeReason(), PurchaseOrderChangeStatus.of(row.status()),
                row.version());
    }
}
