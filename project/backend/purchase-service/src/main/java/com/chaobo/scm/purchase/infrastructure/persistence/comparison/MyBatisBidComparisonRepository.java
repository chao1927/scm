package com.chaobo.scm.purchase.infrastructure.persistence.comparison;

import com.chaobo.scm.purchase.domain.comparison.BidCandidate;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonAggregate;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonRepository;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisBidComparisonRepository implements BidComparisonRepository {
    private final BidComparisonMapper mapper;

    public MyBatisBidComparisonRepository(BidComparisonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<BidComparisonAggregate> findByNo(String compareNo) {
        return Optional.ofNullable(mapper.findByNo(compareNo)).map(this::aggregate);
    }

    @Override
    public void save(BidComparisonAggregate aggregate, long operatorId) {
        var existed = mapper.findByNo(aggregate.compareNo()) != null;
        if (existed) {
            mapper.updateHeader(
                    aggregate.id(),
                    aggregate.status().code(),
                    aggregate.awardedCandidateId(),
                    aggregate.decisionReason(),
                    aggregate.decidedBy(),
                    aggregate.decidedAt(),
                    aggregate.version(),
                    operatorId);
            mapper.deleteCandidates(aggregate.id());
        } else {
            mapper.insertHeader(
                    aggregate.id(),
                    aggregate.compareNo(),
                    aggregate.rfqNo(),
                    aggregate.purchaseOrgId(),
                    aggregate.currency(),
                    aggregate.status().code(),
                    aggregate.awardedCandidateId(),
                    aggregate.decisionReason(),
                    aggregate.decidedBy(),
                    aggregate.decidedAt(),
                    aggregate.version(),
                    operatorId);
        }
        for (BidCandidate candidate : aggregate.candidates()) {
            mapper.insertCandidate(new BidComparisonMapper.CandidateRow(
                    candidate.candidateId(),
                    aggregate.id(),
                    candidate.supplierId(),
                    candidate.supplierName(),
                    candidate.quoteNo(),
                    candidate.skuCode(),
                    candidate.quoteQty(),
                    candidate.unitPrice(),
                    candidate.taxRate(),
                    candidate.deliveryDays(),
                    candidate.supplierScore(),
                    candidate.transportScore(),
                    candidate.estimatedFreightCost(),
                    candidate.totalCost(),
                    candidate.compositeScore(),
                    candidate.awarded()));
        }
    }

    private BidComparisonAggregate aggregate(BidComparisonMapper.HeaderRow row) {
        var candidates = mapper.findCandidates(row.id()).stream()
                .map(candidate -> new BidCandidate(
                        candidate.candidateId(),
                        candidate.supplierId(),
                        candidate.supplierName(),
                        candidate.quoteNo(),
                        candidate.skuCode(),
                        candidate.quoteQty(),
                        candidate.unitPrice(),
                        candidate.taxRate(),
                        candidate.deliveryDays(),
                        candidate.supplierScore(),
                        candidate.transportScore(),
                        candidate.estimatedFreightCost(),
                        candidate.awarded()))
                .toList();
        return new BidComparisonAggregate(
                row.id(),
                row.compareNo(),
                row.rfqNo(),
                row.purchaseOrgId(),
                row.currency(),
                BidComparisonStatus.of(row.status()),
                row.awardedCandidateId(),
                row.decisionReason(),
                row.decidedBy(),
                row.decidedAt(),
                row.version(),
                candidates);
    }
}
