package com.chaobo.scm.purchase.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InboundTrackingAggregate {
    private final long id;
    private final String inboundNo;
    private final String orderNo;
    private final String asnNo;
    private final long supplierId;
    private final long purchaseOrgId;
    private final String warehouseCode;
    private final String skuCode;
    private BigDecimal notifiedQty;
    private BigDecimal receivedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal unqualifiedQty;
    private BigDecimal putawayQty;
    private InboundStatus status;
    private String exceptionReason;
    private int version;
    private final List<DomainEvent> events = new ArrayList<>();

    public InboundTrackingAggregate(long id, String inboundNo, String orderNo, String asnNo, long supplierId,
                                    long purchaseOrgId, String warehouseCode, String skuCode, BigDecimal notifiedQty,
                                    BigDecimal receivedQty, BigDecimal qualifiedQty, BigDecimal unqualifiedQty,
                                    BigDecimal putawayQty, InboundStatus status, String exceptionReason, int version) {
        if (orderNo == null || orderNo.isBlank() || asnNo == null || asnNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "PO和ASN不能为空");
        }
        if (notifiedQty == null || notifiedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "通知数量必须大于0");
        }
        this.id = id;
        this.inboundNo = inboundNo;
        this.orderNo = orderNo;
        this.asnNo = asnNo;
        this.supplierId = supplierId;
        this.purchaseOrgId = purchaseOrgId;
        this.warehouseCode = warehouseCode;
        this.skuCode = skuCode;
        this.notifiedQty = notifiedQty;
        this.receivedQty = zero(receivedQty);
        this.qualifiedQty = zero(qualifiedQty);
        this.unqualifiedQty = zero(unqualifiedQty);
        this.putawayQty = zero(putawayQty);
        this.status = status;
        this.exceptionReason = exceptionReason;
        this.version = version;
    }

    public static InboundTrackingAggregate recordAsn(String orderNo, String asnNo, long supplierId, long purchaseOrgId,
                                                     String warehouseCode, String skuCode, BigDecimal notifiedQty,
                                                     IdentifierGenerator ids) {
        var aggregate = new InboundTrackingAggregate(ids.nextId(), ids.nextCode("INB"), orderNo, asnNo, supplierId,
                purchaseOrgId, warehouseCode, skuCode, notifiedQty, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, InboundStatus.ASN_RECORDED, null, 0);
        aggregate.raise("PurchaseAsnRecorded", Map.of());
        return aggregate;
    }

    public void syncWms(BigDecimal received, BigDecimal qualified, BigDecimal unqualified, BigDecimal putaway,
                        String reason, IdentifierGenerator ids) {
        requireNonNegative(received, "收货数量");
        requireNonNegative(qualified, "合格数量");
        requireNonNegative(unqualified, "不合格数量");
        requireNonNegative(putaway, "上架数量");
        if (received.compareTo(notifiedQty) > 0) {
            status = InboundStatus.EXCEPTION;
            exceptionReason = "WMS数量超过ASN通知数量";
            raise("PurchaseInboundExceptionRaised", Map.of("reason", exceptionReason));
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, exceptionReason);
        }
        version++;
        this.receivedQty = received;
        this.qualifiedQty = qualified;
        this.unqualifiedQty = unqualified;
        this.putawayQty = putaway;
        if (putaway.signum() > 0) {
            status = InboundStatus.PUTAWAY;
            raise("PurchaseGoodsPutawayCompleted", Map.of("syncReason", Objects.requireNonNullElse(reason, "")));
        } else if (qualified.add(unqualified).signum() > 0) {
            status = InboundStatus.INSPECTED;
            raise("PurchaseInspectionCompleted", Map.of("syncReason", Objects.requireNonNullElse(reason, "")));
        } else if (received.signum() > 0) {
            status = InboundStatus.RECEIVED;
            raise("PurchaseGoodsReceived", Map.of("syncReason", Objects.requireNonNullElse(reason, "")));
        }
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("inboundId", id);
        payload.put("inboundNo", inboundNo);
        payload.put("orderNo", orderNo);
        payload.put("asnNo", asnNo);
        payload.put("supplierId", supplierId);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("warehouseCode", Objects.requireNonNullElse(warehouseCode, ""));
        payload.put("skuCode", Objects.requireNonNullElse(skuCode, ""));
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType,
                "INBOUND_TRACKING", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private static void requireNonNegative(BigDecimal value, String name) {
        if (value == null || value.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, name + "不能小于0");
        }
    }

    public long id() { return id; }
    public String inboundNo() { return inboundNo; }
    public String orderNo() { return orderNo; }
    public String asnNo() { return asnNo; }
    public long supplierId() { return supplierId; }
    public long purchaseOrgId() { return purchaseOrgId; }
    public String warehouseCode() { return warehouseCode; }
    public String skuCode() { return skuCode; }
    public BigDecimal notifiedQty() { return notifiedQty; }
    public BigDecimal receivedQty() { return receivedQty; }
    public BigDecimal qualifiedQty() { return qualifiedQty; }
    public BigDecimal unqualifiedQty() { return unqualifiedQty; }
    public BigDecimal putawayQty() { return putawayQty; }
    public InboundStatus status() { return status; }
    public String exceptionReason() { return exceptionReason; }
    public int version() { return version; }
}
