package com.chaobo.scm.purchase.infrastructure.persistence.requisition;

import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionAggregate;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionLine;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionRepository;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisPurchaseRequisitionRepository implements PurchaseRequisitionRepository {
    private final PurchaseRequisitionMapper mapper;

    public MyBatisPurchaseRequisitionRepository(PurchaseRequisitionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<PurchaseRequisitionAggregate> findById(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::aggregate);
    }

    @Override
    public Optional<PurchaseRequisitionAggregate> findByNo(String requisitionNo) {
        return Optional.ofNullable(mapper.findByNo(requisitionNo)).map(this::aggregate);
    }

    @Override
    public void save(PurchaseRequisitionAggregate aggregate, long operatorId) {
        var existed = mapper.findById(aggregate.id()) != null;
        if (existed) {
            mapper.updateHeader(
                    aggregate.id(),
                    aggregate.status().code(),
                    aggregate.reason(),
                    aggregate.version(),
                    operatorId);
            mapper.deleteLines(aggregate.id());
        } else {
            mapper.insertHeader(
                    aggregate.id(),
                    aggregate.requisitionNo(),
                    aggregate.applicantId(),
                    aggregate.purchaseOrgId(),
                    aggregate.demandDepartmentId(),
                    aggregate.status().code(),
                    aggregate.reason(),
                    aggregate.version(),
                    operatorId);
        }
        for (PurchaseRequisitionLine line : aggregate.lines()) {
            mapper.insertLine(new PurchaseRequisitionMapper.LineRow(
                    line.lineId(),
                    aggregate.id(),
                    line.skuCode(),
                    line.requestedQty(),
                    line.approvedQty(),
                    line.convertedQty(),
                    line.purchaseUnit(),
                    line.requiredDate(),
                    line.remark()));
        }
    }

    private PurchaseRequisitionAggregate aggregate(PurchaseRequisitionMapper.HeaderRow row) {
        var lines = mapper.findLines(row.id()).stream()
                .map(line -> new PurchaseRequisitionLine(
                        line.lineId(),
                        line.skuCode(),
                        line.requestedQty(),
                        line.approvedQty(),
                        line.convertedQty(),
                        line.purchaseUnit(),
                        line.requiredDate(),
                        line.remark()))
                .toList();
        return new PurchaseRequisitionAggregate(
                row.id(),
                row.requisitionNo(),
                row.applicantId(),
                row.purchaseOrgId(),
                row.demandDepartmentId(),
                PurchaseRequisitionStatus.of(row.status()),
                row.reason(),
                row.version(),
                lines);
    }
}
