package com.chaobo.scm.purchase.domain.comparison;

import java.util.Optional;

public interface BidComparisonRepository {

    Optional<BidComparisonAggregate> findByNo(String compareNo);

    void save(BidComparisonAggregate aggregate, long operatorId);
}
