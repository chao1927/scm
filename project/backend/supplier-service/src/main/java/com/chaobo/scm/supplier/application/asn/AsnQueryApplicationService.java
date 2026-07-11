package com.chaobo.scm.supplier.application.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.AsnRepository;
import com.chaobo.scm.common.api.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsnQueryApplicationService {
    private final AsnRepository repository;
    private final AsnReadModelPort readModelPort;

    public AsnQueryApplicationService(AsnRepository repository, AsnReadModelPort readModelPort) {
        this.repository = repository;
        this.readModelPort = readModelPort;
    }

    @Transactional(readOnly = true)
    public PageResult<AsnSummaryView> page(Long requestedSupplierId, Long supplierScopeId,
                                           Integer status, String keyword, int pageNo, int pageSize) {
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        Long effectiveSupplierId = supplierScopeId == null ? requestedSupplierId : supplierScopeId;
        return readModelPort.page(effectiveSupplierId, status, keyword, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public AsnDetailView detail(long asnId, Long supplierScopeId) {
        AsnAggregate aggregate = repository.findById(asnId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "ASN 不存在"));
        if (supplierScopeId != null && supplierScopeId != aggregate.supplierId()) {
            // 对越权数据返回 404，避免泄露其他供应商的记录是否存在。
            throw new BusinessException(ErrorCode.NOT_FOUND, "ASN 不存在");
        }
        var shipment = aggregate.shipmentInfo();
        return new AsnDetailView(aggregate.asnId(), aggregate.asnNo(), aggregate.purchaseOrderId(),
                aggregate.supplierId(), aggregate.warehouseId(), aggregate.estimatedArrivalAt(),
                aggregate.status().code(), aggregate.status().label(),
                shipment == null ? null : shipment.shippedAt(),
                shipment == null ? null : shipment.carrierName(),
                shipment == null ? null : shipment.trackingNo(), aggregate.cancelReason(),
                aggregate.version(), aggregate.lines().stream()
                .map(line -> new AsnDetailView.LineView(line.lineId(), line.skuCode(),
                        line.plannedQuantity(), line.receivedQuantity(), line.batchNo(),
                        line.productionDate(), line.expireDate()))
                .toList());
    }
}
