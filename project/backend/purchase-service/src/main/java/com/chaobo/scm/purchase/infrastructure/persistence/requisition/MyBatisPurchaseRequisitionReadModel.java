package com.chaobo.scm.purchase.infrastructure.persistence.requisition;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.requisition.PurchaseRequisitionReadModelPort;
import com.chaobo.scm.purchase.application.requisition.PurchaseRequisitionView;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisPurchaseRequisitionReadModel implements PurchaseRequisitionReadModelPort {
    private final PurchaseRequisitionMapper mapper;
    private final PurchaseRequisitionQueryMapper queryMapper;

    public MyBatisPurchaseRequisitionReadModel(
            PurchaseRequisitionMapper mapper,
            PurchaseRequisitionQueryMapper queryMapper) {
        this.mapper = mapper;
        this.queryMapper = queryMapper;
    }

    @Override
    public PageResult<PurchaseRequisitionView> page(
            Long purchaseOrgId,
            Integer status,
            String keyword,
            int pageNo,
            int pageSize) {
        var total = queryMapper.count(purchaseOrgId, status, keyword);
        var records = queryMapper.page(purchaseOrgId, status, keyword, (pageNo - 1) * pageSize, pageSize)
                .stream()
                .map(this::view)
                .toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<PurchaseRequisitionView> detail(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::view);
    }

    private PurchaseRequisitionView view(PurchaseRequisitionMapper.HeaderRow row) {
        var status = PurchaseRequisitionStatus.of(row.status());
        var lines = mapper.findLines(row.id()).stream()
                .map(line -> new PurchaseRequisitionView.Line(
                        line.lineId(),
                        line.skuCode(),
                        line.requestedQty(),
                        line.approvedQty(),
                        line.convertedQty(),
                        line.purchaseUnit(),
                        line.requiredDate(),
                        line.remark()))
                .toList();
        return new PurchaseRequisitionView(
                row.id(),
                row.requisitionNo(),
                row.applicantId(),
                row.purchaseOrgId(),
                row.demandDepartmentId(),
                row.status(),
                status.label(),
                row.reason(),
                row.version(),
                row.createdAt(),
                row.updatedAt(),
                lines);
    }
}
