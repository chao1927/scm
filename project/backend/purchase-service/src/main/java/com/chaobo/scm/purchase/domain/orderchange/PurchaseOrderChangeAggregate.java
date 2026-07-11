package com.chaobo.scm.purchase.domain.orderchange;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrderChangeAggregate {
    private final long id;
    private final String changeNo;
    private final String orderNo;
    private final int changeType;
    private final String beforeSnapshot;
    private final String afterSnapshot;
    private final String changeReason;
    private PurchaseOrderChangeStatus status;
    private int version;
    private final List<DomainEvent> events = new ArrayList<>();

    public PurchaseOrderChangeAggregate(long id, String changeNo, String orderNo, int changeType, String beforeSnapshot,
                                        String afterSnapshot, String changeReason, PurchaseOrderChangeStatus status,
                                        int version) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单号不能为空");
        }
        if (changeReason == null || changeReason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "变更原因不能为空");
        }
        this.id = id;
        this.changeNo = changeNo;
        this.orderNo = orderNo;
        this.changeType = changeType;
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
        this.changeReason = changeReason;
        this.status = status;
        this.version = version;
    }

    public static PurchaseOrderChangeAggregate create(String orderNo, int changeType, String beforeSnapshot,
                                                      String afterSnapshot, String changeReason, IdentifierGenerator ids) {
        var aggregate = new PurchaseOrderChangeAggregate(ids.nextId(), ids.nextCode("POC"), orderNo, changeType,
                beforeSnapshot, afterSnapshot, changeReason, PurchaseOrderChangeStatus.PENDING_APPROVAL, 0);
        aggregate.raise("PurchaseOrderChangeCreated", Map.of());
        return aggregate;
    }

    public void approve(boolean approved, IdentifierGenerator ids) {
        if (status != PurchaseOrderChangeStatus.PENDING_APPROVAL) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前变更单状态不能审批");
        }
        version++;
        status = approved ? PurchaseOrderChangeStatus.EFFECTIVE : PurchaseOrderChangeStatus.REJECTED;
        raise(approved ? "PurchaseOrderChangeEffective" : "PurchaseOrderChangeRejected", Map.of());
    }

    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("changeId", id);
        payload.put("changeNo", changeNo);
        payload.put("orderNo", orderNo);
        payload.put("changeType", changeType);
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType,
                "PURCHASE_ORDER_CHANGE", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    public long id() { return id; }
    public String changeNo() { return changeNo; }
    public String orderNo() { return orderNo; }
    public int changeType() { return changeType; }
    public String beforeSnapshot() { return beforeSnapshot; }
    public String afterSnapshot() { return afterSnapshot; }
    public String changeReason() { return changeReason; }
    public PurchaseOrderChangeStatus status() { return status; }
    public int version() { return version; }
}
