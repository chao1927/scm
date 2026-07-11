package com.chaobo.scm.supplier.application.workbench;

import com.chaobo.scm.supplier.infrastructure.persistence.workbench.SupplierWorkbenchMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class SupplierWorkbenchApplicationService {
    private final SupplierWorkbenchMapper mapper;

    public SupplierWorkbenchApplicationService(SupplierWorkbenchMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public SupplierWorkbenchView summary(Long supplierId, Long supplierScopeId, int recentDays) {
        if (recentDays < 1 || recentDays > 365) {
            throw new com.chaobo.scm.common.error.BusinessException(
                    com.chaobo.scm.common.error.ErrorCode.VALIDATION_FAILED, "统计天数必须在1到365之间");
        }
        var scopedSupplierId = supplierScopeId == null ? supplierId : supplierScopeId;
        var since = OffsetDateTime.now().minusDays(recentDays);
        return new SupplierWorkbenchView(
                mapper.pendingQuotes(scopedSupplierId),
                mapper.pendingPurchaseOrderConfirms(scopedSupplierId),
                mapper.pendingAsns(scopedSupplierId),
                mapper.pendingReconciliations(scopedSupplierId),
                mapper.pendingRectifications(scopedSupplierId),
                mapper.openWarnings(scopedSupplierId),
                mapper.failedEvents(),
                mapper.openReturns(scopedSupplierId),
                mapper.latestScore(scopedSupplierId, since),
                OffsetDateTime.now(),
                mapper.todoGroups(scopedSupplierId),
                mapper.warningGroups(scopedSupplierId));
    }
}
