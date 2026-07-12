package com.chaobo.scm.purchase.domain.order;

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

public class PurchaseOrderAggregate {
    private final long id;
    private final String orderNo;
    private final int purchaseType;
    private final long supplierId;
    private final String supplierCode;
    private final String supplierName;
    private final long purchaseOrgId;
    private final String warehouseCode;
    private final String currency;
    private PurchaseOrderStatus status;
    private int versionNo;
    private int version;
    private OffsetDateTime releasedAt;
    private String cancelReason;
    private final List<PurchaseOrderLine> lines;
    private final List<DomainEvent> events = new ArrayList<>();

    public PurchaseOrderAggregate(long id, String orderNo, int purchaseType, long supplierId, String supplierCode,
                                  String supplierName, long purchaseOrgId, String warehouseCode, String currency,
                                  PurchaseOrderStatus status, int versionNo, int version, OffsetDateTime releasedAt,
                                  String cancelReason, List<PurchaseOrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单必须至少包含一行商品");
        }
        this.id = id;
        this.orderNo = orderNo;
        this.purchaseType = purchaseType;
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.purchaseOrgId = purchaseOrgId;
        this.warehouseCode = warehouseCode;
        this.currency = currency;
        this.status = status;
        this.versionNo = versionNo;
        this.version = version;
        this.releasedAt = releasedAt;
        this.cancelReason = cancelReason;
        this.lines = new ArrayList<>(lines);
        assertNoDuplicateSku();
    }

    public static PurchaseOrderAggregate create(int purchaseType, long supplierId, String supplierCode, String supplierName,
                                                long purchaseOrgId, String warehouseCode, String currency,
                                                List<PurchaseOrderLine> lines, IdentifierGenerator ids) {
        var aggregate = new PurchaseOrderAggregate(ids.nextId(), ids.nextCode("PO"), purchaseType, supplierId,
                supplierCode, supplierName, purchaseOrgId, warehouseCode, currency, PurchaseOrderStatus.DRAFT,
                1, 0, null, null, lines);
        aggregate.raise("PurchaseOrderCreated", Map.of());
        return aggregate;
    }

    public void submit(IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.REJECTED);
        touch();
        status = PurchaseOrderStatus.SUBMITTED;
        raise("PurchaseOrderSubmitted", Map.of());
    }

    public void approve(boolean approved, String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUBMITTED);
        touch();
        if (approved) {
            status = PurchaseOrderStatus.APPROVED;
            raise("PurchaseOrderApproved", Map.of());
        } else {
            if (reason == null || reason.isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "驳回原因不能为空");
            }
            status = PurchaseOrderStatus.REJECTED;
            raise("PurchaseOrderRejected", Map.of("reason", reason));
        }
    }

    public void publish(String publishMode, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.APPROVED);
        touch();
        status = PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM;
        releasedAt = OffsetDateTime.now();
        raise("PurchaseOrderPublished", Map.of("publishMode", Objects.requireNonNullElse(publishMode, "EVENT")));
    }

    public void cancel(String reason, IdentifierGenerator ids) {
        if (receivedQuantity().signum() > 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "采购订单已有入库执行，不能直接取消");
        }
        if (status == PurchaseOrderStatus.CANCELLED || status == PurchaseOrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前采购订单状态不能取消");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "取消原因不能为空");
        }
        touch();
        status = PurchaseOrderStatus.CANCELLED;
        cancelReason = reason;
        raise("PurchaseOrderCancelled", Map.of("reason", reason));
    }

    public void closeRemaining(String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUPPLIER_CONFIRMED, PurchaseOrderStatus.PARTIALLY_INBOUNDED,
                PurchaseOrderStatus.SUPPLIER_DIFF, PurchaseOrderStatus.SUPPLIER_REJECTED);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "关闭剩余数量原因不能为空");
        }
        touch();
        versionNo++;
        status = PurchaseOrderStatus.CLOSED;
        raise("PurchaseOrderClosed", Map.of("reason", reason));
    }

    /**
     * 供应商确认是采购订单发布后的外部业务事实。事件先由 Inbox 幂等保护，
     * 再调用本方法推进订单，避免供应商门户直接改写采购订单数据。
     */
    public void recordSupplierConfirmation(String remark, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        touch();
        status = PurchaseOrderStatus.SUPPLIER_CONFIRMED;
        raise("SupplierOrderConfirmationRecorded", optional("remark", remark));
    }

    public void recordSupplierRejection(String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "供应商拒绝原因不能为空");
        }
        touch();
        status = PurchaseOrderStatus.SUPPLIER_REJECTED;
        raise("SupplierOrderRejectionRecorded", Map.of("reason", reason));
    }

    public void recordSupplierDifference(String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        touch();
        status = PurchaseOrderStatus.SUPPLIER_DIFF;
        raise("SupplierOrderDifferenceRecorded", optional("reason", reason));
    }

    public void acceptSupplierDifference(String comment, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUPPLIER_DIFF);
        touch();
        status = PurchaseOrderStatus.SUPPLIER_CONFIRMED;
        raise("SupplierOrderDifferenceAccepted", optional("comment", comment));
    }

    public void restartSupplierNegotiation(String requirement, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUPPLIER_DIFF, PurchaseOrderStatus.SUPPLIER_REJECTED);
        if (requirement == null || requirement.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "重新协商要求不能为空");
        }
        touch();
        status = PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM;
        raise("SupplierOrderRenegotiationRequested", Map.of("requirement", requirement));
    }

    public void applyLineQtyChanges(Map<Long, BigDecimal> lineQtyChanges, IdentifierGenerator ids) {
        if (status == PurchaseOrderStatus.COMPLETED || status == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "已完成或已取消订单不能变更");
        }
        if (lineQtyChanges == null || lineQtyChanges.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "变更行不能为空");
        }
        for (Map.Entry<Long, BigDecimal> entry : lineQtyChanges.entrySet()) {
            line(entry.getKey()).changeQty(entry.getValue());
        }
        touch();
        versionNo++;
        raise("PurchaseOrderChangeEffective", Map.of("changeType", "QTY"));
    }

    public BigDecimal totalAmount() {
        return lines.stream().map(PurchaseOrderLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal taxAmount() {
        return lines.stream().map(PurchaseOrderLine::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal taxIncludedAmount() {
        return totalAmount().add(taxAmount());
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private PurchaseOrderLine line(long lineId) {
        return lines.stream().filter(line -> line.lineId() == lineId).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单行不存在"));
    }

    private BigDecimal receivedQuantity() {
        return lines.stream().map(PurchaseOrderLine::receivedQty).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void ensureStatus(PurchaseOrderStatus... allowed) {
        for (PurchaseOrderStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前采购订单状态不允许执行该操作");
    }

    private void touch() {
        version++;
    }

    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("orderId", id);
        payload.put("orderNo", orderNo);
        payload.put("supplierId", supplierId);
        payload.put("supplierCode", supplierCode);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("warehouseCode", Objects.requireNonNullElse(warehouseCode, ""));
        payload.put("currency", currency);
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType,
                "PURCHASE_ORDER", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    private static Map<String, Object> optional(String key, String value) {
        return value == null || value.isBlank() ? Map.of() : Map.of(key, value);
    }

    private void assertNoDuplicateSku() {
        var keys = new java.util.HashSet<String>();
        for (PurchaseOrderLine line : lines) {
            if (!keys.add(line.skuCode())) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "采购订单行SKU不能重复");
            }
        }
    }

    public long id() { return id; }
    public String orderNo() { return orderNo; }
    public int purchaseType() { return purchaseType; }
    public long supplierId() { return supplierId; }
    public String supplierCode() { return supplierCode; }
    public String supplierName() { return supplierName; }
    public long purchaseOrgId() { return purchaseOrgId; }
    public String warehouseCode() { return warehouseCode; }
    public String currency() { return currency; }
    public PurchaseOrderStatus status() { return status; }
    public int versionNo() { return versionNo; }
    public int version() { return version; }
    public OffsetDateTime releasedAt() { return releasedAt; }
    public String cancelReason() { return cancelReason; }
    public List<PurchaseOrderLine> lines() { return List.copyOf(lines); }
}
