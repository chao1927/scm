package com.chaobo.scm.purchase.infrastructure.persistence.comparison;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.comparison.BidComparisonReadModelPort;
import com.chaobo.scm.purchase.application.comparison.BidComparisonView;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisBidComparisonReadModel implements BidComparisonReadModelPort {
    private final BidComparisonMapper mapper;
    private final BidComparisonQueryMapper queryMapper;

    public MyBatisBidComparisonReadModel(BidComparisonMapper mapper, BidComparisonQueryMapper queryMapper) {
        this.mapper = mapper;
        this.queryMapper = queryMapper;
    }

    @Override
    public PageResult<BidComparisonView> page(Long purchaseOrgId, Integer status, String rfqNo, int pageNo, int pageSize) {
        var total = queryMapper.count(purchaseOrgId, status, rfqNo);
        var records = queryMapper.page(purchaseOrgId, status, rfqNo, (pageNo - 1) * pageSize, pageSize)
                .stream()
                .map(this::view)
                .toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<BidComparisonView> detail(String compareNo) {
        return Optional.ofNullable(mapper.findByNo(compareNo)).map(this::view);
    }

    private BidComparisonView view(BidComparisonMapper.HeaderRow row) {
        var status = BidComparisonStatus.of(row.status());
        var candidates = mapper.findCandidates(row.id()).stream()
                .map(candidate -> new BidComparisonView.Candidate(
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
                        candidate.totalCost(),
                        candidate.compositeScore(),
                        candidate.awarded()))
                .toList();
        return new BidComparisonView(
                row.id(),
                row.compareNo(),
                row.rfqNo(),
                row.purchaseOrgId(),
                row.currency(),
                row.status(),
                status.label(),
                row.awardedCandidateId(),
                row.decisionReason(),
                row.decidedBy(),
                row.decidedAt(),
                row.version(),
                candidates);
    }
}
