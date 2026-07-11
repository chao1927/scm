package com.chaobo.scm.supplier.application.report;

import com.chaobo.scm.supplier.infrastructure.persistence.report.SupplierReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierReportApplicationService {
    private final SupplierReportMapper mapper;

    public SupplierReportApplicationService(SupplierReportMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public SupplierReportViews.Fulfillment fulfillment(Long supplierId, Long supplierScopeId) {
        return mapper.fulfillment(supplierScopeId == null ? supplierId : supplierScopeId);
    }

    @Transactional(readOnly = true)
    public SupplierReportViews.ExceptionOverview exceptions(Long supplierId, Long supplierScopeId) {
        return mapper.exceptions(supplierScopeId == null ? supplierId : supplierScopeId);
    }
}
