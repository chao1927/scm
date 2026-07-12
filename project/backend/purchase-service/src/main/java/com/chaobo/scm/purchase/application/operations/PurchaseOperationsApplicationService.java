package com.chaobo.scm.purchase.application.operations;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.PurchaseOperationsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseOperationsApplicationService {
    private final PurchaseOperationsMapper mapper;

    public PurchaseOperationsApplicationService(PurchaseOperationsMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOperationsViews.FailedEvent> failedEvents() {
        return mapper.failedInboundEvents(100);
    }
}
