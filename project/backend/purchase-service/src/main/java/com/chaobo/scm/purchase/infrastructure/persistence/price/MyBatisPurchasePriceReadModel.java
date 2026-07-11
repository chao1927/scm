package com.chaobo.scm.purchase.infrastructure.persistence.price;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.price.PurchasePriceReadModelPort;
import com.chaobo.scm.purchase.application.price.PurchasePriceView;
import com.chaobo.scm.purchase.domain.price.PurchasePriceStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisPurchasePriceReadModel implements PurchasePriceReadModelPort {
    private final PurchasePriceMapper mapper;
    private final PurchasePriceQueryMapper queryMapper;

    public MyBatisPurchasePriceReadModel(PurchasePriceMapper mapper, PurchasePriceQueryMapper queryMapper) {
        this.mapper = mapper;
        this.queryMapper = queryMapper;
    }

    @Override
    public PageResult<PurchasePriceView> page(
            Long purchaseOrgId,
            Long supplierId,
            String skuCode,
            String currency,
            Integer status,
            int pageNo,
            int pageSize) {
        var total = queryMapper.count(purchaseOrgId, supplierId, skuCode, currency, status);
        var records = queryMapper.page(purchaseOrgId, supplierId, skuCode, currency, status, (pageNo - 1) * pageSize, pageSize)
                .stream()
                .map(this::view)
                .toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<PurchasePriceView> detail(String priceNo) {
        return Optional.ofNullable(mapper.findByNo(priceNo)).map(this::view);
    }

    private PurchasePriceView view(PurchasePriceMapper.PriceRow row) {
        var status = PurchasePriceStatus.of(row.status());
        return new PurchasePriceView(
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
                row.status(),
                status.label(),
                row.version());
    }
}
