package com.chaobo.scm.purchase.domain.price;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PurchasePriceAggregate {
    private final long id;
    private final String priceNo;
    private final long supplierId;
    private final String skuCode;
    private final long purchaseOrgId;
    private final int priceType;
    private final String currency;
    private final BigDecimal unitPrice;
    private final BigDecimal taxRate;
    private final BigDecimal taxIncludedPrice;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
    private final String sourceType;
    private final String sourceNo;
    private PurchasePriceStatus status;
    private int version;
    private final List<DomainEvent> events = new ArrayList<>();

    public PurchasePriceAggregate(
            long id,
            String priceNo,
            long supplierId,
            String skuCode,
            long purchaseOrgId,
            int priceType,
            String currency,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal taxIncludedPrice,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String sourceType,
            String sourceNo,
            PurchasePriceStatus status,
            int version) {
        validate(supplierId, skuCode, purchaseOrgId, priceType, currency, unitPrice, taxRate, effectiveFrom, effectiveTo);
        this.id = id;
        this.priceNo = priceNo;
        this.supplierId = supplierId;
        this.skuCode = skuCode;
        this.purchaseOrgId = purchaseOrgId;
        this.priceType = priceType;
        this.currency = currency;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate;
        this.taxIncludedPrice = taxIncludedPrice == null ? taxIncluded(unitPrice, taxRate) : taxIncludedPrice;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.sourceType = sourceType;
        this.sourceNo = sourceNo;
        this.status = status;
        this.version = version;
    }

    public static PurchasePriceAggregate create(
            long supplierId,
            String skuCode,
            long purchaseOrgId,
            int priceType,
            String currency,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String sourceType,
            String sourceNo,
            IdentifierGenerator ids) {
        var aggregate = new PurchasePriceAggregate(
                ids.nextId(),
                ids.nextCode("PRICE"),
                supplierId,
                skuCode,
                purchaseOrgId,
                priceType,
                currency,
                unitPrice,
                taxRate,
                null,
                effectiveFrom,
                effectiveTo,
                sourceType,
                sourceNo,
                PurchasePriceStatus.ACTIVE,
                0);
        aggregate.raise("PurchasePriceActivated");
        return aggregate;
    }

    public void disable(IdentifierGenerator ids) {
        if (status == PurchasePriceStatus.DISABLED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "采购价格已停用");
        }
        touch();
        this.status = PurchasePriceStatus.DISABLED;
        raise("PurchasePriceDisabled");
    }

    public boolean overlaps(LocalDate from, LocalDate to) {
        var thisTo = effectiveTo == null ? LocalDate.MAX : effectiveTo;
        var targetTo = to == null ? LocalDate.MAX : to;
        return !thisTo.isBefore(from) && !targetTo.isBefore(effectiveFrom);
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private void touch() {
        version++;
    }

    private void raise(String eventType) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("priceId", id);
        payload.put("priceNo", priceNo);
        payload.put("supplierId", supplierId);
        payload.put("skuCode", skuCode);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("currency", currency);
        payload.put("unitPrice", unitPrice.toPlainString());
        payload.put("taxRate", taxRate.toPlainString());
        payload.put("status", status.code());
        payload.put("version", version);
        payload.put("sourceType", Objects.requireNonNullElse(sourceType, ""));
        payload.put("sourceNo", Objects.requireNonNullElse(sourceNo, ""));
        events.add(new DomainEvent(
                0,
                "PUR-" + eventType + "-" + id + "-" + version,
                eventType,
                "PURCHASE_PRICE",
                Long.toString(id),
                version,
                OffsetDateTime.now(),
                payload));
    }

    private static void validate(
            long supplierId,
            String skuCode,
            long purchaseOrgId,
            int priceType,
            String currency,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        if (supplierId <= 0 || purchaseOrgId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "供应商和采购组织不能为空");
        }
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "SKU不能为空");
        }
        if (priceType < 1 || priceType > 3) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "价格类型不合法");
        }
        if (currency == null || currency.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "币种不能为空");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "未税单价不能小于0");
        }
        if (taxRate == null || taxRate.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "税率不能小于0");
        }
        if (effectiveFrom == null || effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "价格有效期不合法");
        }
    }

    private static BigDecimal taxIncluded(BigDecimal unitPrice, BigDecimal taxRate) {
        return unitPrice.multiply(BigDecimal.ONE.add(taxRate)).setScale(6, RoundingMode.HALF_UP);
    }

    public long id() {
        return id;
    }

    public String priceNo() {
        return priceNo;
    }

    public long supplierId() {
        return supplierId;
    }

    public String skuCode() {
        return skuCode;
    }

    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    public int priceType() {
        return priceType;
    }

    public String currency() {
        return currency;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public BigDecimal taxRate() {
        return taxRate;
    }

    public BigDecimal taxIncludedPrice() {
        return taxIncludedPrice;
    }

    public LocalDate effectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate effectiveTo() {
        return effectiveTo;
    }

    public String sourceType() {
        return sourceType;
    }

    public String sourceNo() {
        return sourceNo;
    }

    public PurchasePriceStatus status() {
        return status;
    }

    public int version() {
        return version;
    }
}
