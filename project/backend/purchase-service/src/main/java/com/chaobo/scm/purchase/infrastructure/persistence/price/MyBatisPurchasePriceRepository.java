package com.chaobo.scm.purchase.infrastructure.persistence.price;

import com.chaobo.scm.purchase.domain.price.PurchasePriceAggregate;
import com.chaobo.scm.purchase.domain.price.PurchasePriceRepository;
import com.chaobo.scm.purchase.domain.price.PurchasePriceStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisPurchasePriceRepository implements PurchasePriceRepository {
    private final PurchasePriceMapper mapper;

    public MyBatisPurchasePriceRepository(PurchasePriceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<PurchasePriceAggregate> findByNo(String priceNo) {
        return Optional.ofNullable(mapper.findByNo(priceNo)).map(this::aggregate);
    }

    @Override
    public List<PurchasePriceAggregate> findActiveOverlaps(
            long supplierId,
            String skuCode,
            long purchaseOrgId,
            String currency,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        return mapper.findActiveOverlaps(supplierId, skuCode, purchaseOrgId, currency, effectiveFrom, effectiveTo)
                .stream()
                .map(this::aggregate)
                .toList();
    }

    @Override
    public void save(PurchasePriceAggregate aggregate, long operatorId) {
        var existed = mapper.findByNo(aggregate.priceNo()) != null;
        if (existed) {
            mapper.updateStatus(aggregate.id(), aggregate.status().code(), aggregate.version(), operatorId);
        } else {
            mapper.insert(row(aggregate), operatorId);
        }
    }

    private PurchasePriceMapper.PriceRow row(PurchasePriceAggregate aggregate) {
        return new PurchasePriceMapper.PriceRow(
                aggregate.id(),
                aggregate.priceNo(),
                aggregate.supplierId(),
                aggregate.skuCode(),
                aggregate.purchaseOrgId(),
                aggregate.priceType(),
                aggregate.currency(),
                aggregate.unitPrice(),
                aggregate.taxRate(),
                aggregate.taxIncludedPrice(),
                aggregate.effectiveFrom(),
                aggregate.effectiveTo(),
                aggregate.sourceType(),
                aggregate.sourceNo(),
                aggregate.status().code(),
                aggregate.version());
    }

    private PurchasePriceAggregate aggregate(PurchasePriceMapper.PriceRow row) {
        return new PurchasePriceAggregate(
                row.id(),
                row.priceNo(),
                row.supplierId(),
                row.skuCode(),
                row.purchaseOrgId(),
                row.priceType(),
                row.currency(),
                row.unitPrice(),
                row.taxRate(),
                row.taxIncludedPrice(),
                row.effectiveFrom(),
                row.effectiveTo(),
                row.sourceType(),
                row.sourceNo(),
                PurchasePriceStatus.of(row.status()),
                row.version());
    }
}
